package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.PendingDonationService;
import com.GiveGrid.store.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/donations")
public class AppliedDonationsController {

    private final PendingDonationService pendingService;
    private final UserService userService;

    public AppliedDonationsController(PendingDonationService pendingService, UserService userService) {
        this.pendingService = pendingService;
        this.userService = userService;
    }

    @GetMapping("/applied")
    public String appliedFor(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";

        User buyer = userService.findByUsername(auth.getName());
        List<PendingDonation> list = pendingService.getForBuyer(buyer);

        // send a safe list
        model.addAttribute("appliedList",
                list != null ? list : new ArrayList<>());

        return "applied-donations";
    }
}
