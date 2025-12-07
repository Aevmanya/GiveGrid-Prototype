package com.GiveGrid.store.controller;

import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.entity.CartItem;
import com.GiveGrid.store.entity.Donation;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.CartService;
import com.GiveGrid.store.service.DonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.GiveGrid.store.entity.PendingDonation;

import java.time.LocalDateTime;
import java.util.Arrays;

@Controller
@RequestMapping("/donate")
public class DonationController {

    private final CartService cartService;
    private final UserService userService;
    private final DonationService donationService;
    private final PendingDonationService pendingDonationService;

    public DonationController(CartService cartService,
                              UserService userService,
                              DonationService donationService,
                              PendingDonationService pendingDonationService) {

        this.cartService = cartService;
        this.userService = userService;
        this.donationService = donationService;
        this.pendingDonationService = pendingDonationService; // ✅ FIX
    }

    // Step 1: show cart items so buyer picks one to donate from
    @GetMapping("/select")
    public String selectDonationItem(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        model.addAttribute("cartItems", cartService.getUserCart(user));

        return "donate-select";
    }

    // Step 2: donation form for chosen cart item
    @GetMapping("/form/{itemId}")
    public String donationForm(@PathVariable Long itemId, Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        CartItem cartItem = cartService.getItemById(itemId);
        if (cartItem == null) return "redirect:/donate/select?notfound";

        model.addAttribute("item", cartItem);
        return "donate-form";
    }

    // Step 3: submit donation (single donation record, aggregated)
    @PostMapping("/submit")
    public String submitDonation(@RequestParam Long itemId,
                                 @RequestParam Integer donateCount,
                                 @RequestParam(name = "condition", required = false) String[] conditions,
                                 Authentication auth,
                                 Model model) {

        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        CartItem cartItem = cartService.getItemById(itemId);

        if (cartItem == null) return "redirect:/donate/select?notfound";

        // inject pendingDonationService
// inside submitDonation(...)
        int maxAllowed = cartItem.getQuantity();
        if (donateCount == null || donateCount < 1 || donateCount > maxAllowed) {
            model.addAttribute("error", "Invalid donation quantity.");
            model.addAttribute("item", cartItem);
            return "donate-form";
        }

// Determine final condition: if all choices same -> use that, otherwise "Mixed"
        String finalCondition = "New";
        if (conditions != null && conditions.length >= donateCount) {
            boolean allSame = true;
            String first = conditions[0];
            for (int i = 1; i < donateCount; i++) {
                if (!first.equals(conditions[i])) {
                    allSame = false;
                    break;
                }
            }
            finalCondition = allSame ? first : "Mixed";
        }

// create pending donation record with quantity = donateCount
        PendingDonation pd = new PendingDonation();
        pd.setBuyer(user);
        pd.setSeller(cartItem.getProduct().getSeller());
        pd.setProduct(cartItem.getProduct());
        pd.setQuantity(donateCount);
        pd.setCondition(finalCondition);
        pendingDonationService.save(pd);

        model.addAttribute("msg", "Donation request submitted. Waiting for seller approval.");
        return "donate-success";

    }
}