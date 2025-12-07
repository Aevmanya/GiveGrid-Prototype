package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.DonationRepository;
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
    private DonationRepository donationRepository;

    // View profile
    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {

        User user = userService.findByUsername(auth.getName());

        model.addAttribute("user", user);

        model.addAttribute("donations", donationRepository.findByUserOrderByDonatedAtDesc(user));

        return "profile";
    }

}
