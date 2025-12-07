package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.service.CartService;
import com.GiveGrid.store.service.ProductService;
import com.GiveGrid.store.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth) {

        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        model.addAttribute("items", cartService.getUserCart(user));

        return "cart";
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId, Authentication auth) {

        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        Product p = productService.getProductById(productId);

        // Prevent adding finished products
        if (p.getQuantity() <= 0) {
            return "redirect:/cart?finished";
        }

        cartService.addToCart(user, p);

        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return "redirect:/cart";
    }
}
