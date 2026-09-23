package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.ProductService;
import com.GiveGrid.store.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerController {
    private final UserService userService;
    private final ProductService productService;
    private final PendingDonationService pendingDonationService;

    public SellerController(UserService userService, ProductService productService, PendingDonationService pendingDonationService) {
        this.userService = userService;
        this.productService = productService;
        this.pendingDonationService = pendingDonationService;
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        User organisation = userService.findByUsername(auth.getName());
        if (organisation == null) return "redirect:/login";
        model.addAttribute("user", organisation);
        model.addAttribute("listingCount", productService.getProductsBySeller(organisation).size());
        model.addAttribute("pendingCount", pendingDonationService.getPendingForSeller(organisation).size());
        model.addAttribute("approvedCount", pendingDonationService.getApprovedForSeller(organisation).size());
        return "seller-dashboard";
    }
}
