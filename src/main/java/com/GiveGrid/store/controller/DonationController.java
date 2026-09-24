package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.CartItem;
import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.CartService;
import com.GiveGrid.store.service.DonationService;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
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
        this.pendingDonationService = pendingDonationService;
    }

    @GetMapping({"/donate/history", "/donations/history"})
    public String donationHistory(Authentication auth, Model model) {
        return renderDonationHistory(auth, model);
    }

    private String renderDonationHistory(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("appliedList", pendingDonationService.getForBuyer(user));
        return "search-results";
    }

    @GetMapping("/donate/select")
    public String selectDonationItem(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartService.getUserCart(user));
        return "donate-select";
    }

    @GetMapping("/donate/form/{itemId}")
    public String donationForm(@PathVariable Long itemId, Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        if (user == null) return "redirect:/login";

        CartItem cartItem = cartService.getItemById(itemId);
        if (cartItem == null || cartItem.getUser() == null ||
                !cartItem.getUser().getId().equals(user.getId())) {
            return "redirect:/donate/select?notfound";
        }

        model.addAttribute("user", user);
        model.addAttribute("item", cartItem);
        return "donate-form";
    }

    @PostMapping("/donate/submit")
    public String submitDonation(@RequestParam Long itemId,
                                 @RequestParam Integer donateCount,
                                 @RequestParam(name = "condition", required = false) String condition,
                                 Authentication auth,
                                 Model model) {

        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        if (user == null) return "redirect:/login";

        CartItem cartItem = cartService.getItemById(itemId);
        if (cartItem == null || cartItem.getUser() == null ||
                !cartItem.getUser().getId().equals(user.getId())) {
            return "redirect:/donate/select?notfound";
        }

        int maxAllowed = cartItem.getQuantity();
        if (donateCount == null || donateCount < 1 || donateCount > maxAllowed) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Invalid donation quantity.");
            model.addAttribute("item", cartItem);
            return "donate-form";
        }

        String finalCondition = (condition == null || condition.isBlank()) ? "New" : condition.trim();

        PendingDonation pd = new PendingDonation();
        pd.setBuyer(user);
        pd.setSeller(cartItem.getProduct().getSeller());
        pd.setProduct(cartItem.getProduct());
        pd.setQuantity(donateCount);
        pd.setCondition(finalCondition);
        pendingDonationService.save(pd);

        model.addAttribute("user", user);
        model.addAttribute("msg", "Donation request submitted. Waiting for seller approval.");
        return "donate-success";
    }
}