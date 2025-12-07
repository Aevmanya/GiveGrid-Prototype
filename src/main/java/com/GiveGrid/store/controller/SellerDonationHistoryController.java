package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class SellerDonationHistoryController {

    private final PendingDonationService pendingService;
    private final UserService userService;

    public SellerDonationHistoryController(PendingDonationService pendingService,
                                           UserService userService) {
        this.pendingService = pendingService;
        this.userService = userService;
    }

    @GetMapping("/seller/donation/history")
    public String history(Authentication auth, Model model) {

        if (auth == null) return "redirect:/login";

        User seller = userService.findByUsername(auth.getName());

        model.addAttribute("historyList",
                pendingService.getApprovedForSeller(seller));

        return "seller-donation-history";
    }
}
