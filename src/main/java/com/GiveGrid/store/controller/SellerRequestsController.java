package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@Controller
@RequestMapping("/seller/requests")
public class SellerRequestsController {

    private final PendingDonationService pendingService;
    private final UserService userService;

    public SellerRequestsController(PendingDonationService pendingService, UserService userService) {
        this.pendingService = pendingService;
        this.userService = userService;
    }

    @GetMapping
    public String listRequests(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        User seller = userService.findByUsername(auth.getName());
        List<PendingDonation> list = pendingService.getPendingForSeller(seller);
        model.addAttribute("requests", list);
        return "seller-requests";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        // optionally verify seller owns the product/seller
        pendingService.approve(id);
        return "redirect:/seller/requests?approved";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        pendingService.reject(id);
        return "redirect:/seller/requests?rejected";
    }
}

