package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // HANDLE SIGNUP
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute User user, Model model) {

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already taken.");
            return "signup";
        }

        if (user.getEmail() != null && userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already used.");
            return "signup";
        }

        // ✔ Correct: encode ONLY here
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userService.save(user);
        return "redirect:/login?registered";
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // SHOW EDIT PROFILE PAGE
    @GetMapping("/account/edit")
    public String editProfileForm(Authentication auth, Model model) {

        if (auth == null) return "redirect:/login";

        User user = userService.findByUsername(auth.getName());
        model.addAttribute("user", user);

        return "edit-account";
    }

    // HANDLE PROFILE EDIT
    @PostMapping("/account/edit")
    public String updateProfile(@ModelAttribute("user") User updated, Authentication auth) {

        if (auth == null) return "redirect:/login";

        User existing = userService.findByUsername(auth.getName());

        // Update editable fields only
        existing.setFullName(updated.getFullName());
        existing.setLocation(updated.getLocation());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());
        existing.setAge(updated.getAge());

        userService.updateProfile(existing);

        return "redirect:/account?updated=true";
    }
}
