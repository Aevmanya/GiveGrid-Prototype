package com.GiveGrid.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerController {

    @GetMapping("/seller/dashboard")
    public String sellerDashboard() {
        return "seller-dashboard";
    }
}


