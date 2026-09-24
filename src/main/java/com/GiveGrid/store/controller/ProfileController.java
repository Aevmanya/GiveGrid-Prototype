package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PendingDonationService pendingDonationService;

    // View profile
    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {

        User user = userService.findByUsername(auth.getName());

        model.addAttribute("user", user);

        java.util.List<com.GiveGrid.store.entity.PendingDonation> allDonations =
                pendingDonationService.getAcceptedForBuyer(user);
        model.addAttribute("donationCount", allDonations.size());
        model.addAttribute("donations", allDonations.stream().limit(3).toList());
        model.addAttribute("requestedDonations", pendingDonationService.getForBuyer(user));

        return "profile";
    }

}
