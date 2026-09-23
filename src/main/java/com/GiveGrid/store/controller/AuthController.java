package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.dao.DataIntegrityViolationException;

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
    public String updateProfile(@ModelAttribute("user") User updated, Authentication auth, Model model) {

        if (auth == null) return "redirect:/login";

        User existing = userService.findByUsername(auth.getName());

        if (!hasText(updated.getFullName())
                || !hasText(updated.getEmail())
                || updated.getAge() == null
                || updated.getAge() < 1
                || updated.getAge() > 120
                || !hasText(updated.getPhone())
                || !hasText(updated.getAddress())
                || !hasText(updated.getLocation())) {
            model.addAttribute("user", existing);
            model.addAttribute("error", "Please complete every personal detail before continuing.");
            return "edit-account";
        }

        String newEmail = updated.getEmail().trim();
        if (!newEmail.equalsIgnoreCase(existing.getEmail() == null ? "" : existing.getEmail().trim())
                && userService.emailExistsForAnotherUser(newEmail, existing.getId())) {
            model.addAttribute("user", existing);
            model.addAttribute("error", "That email address is already in use.");
            return "edit-account";
        }

        // Update editable fields only
        existing.setEmail(newEmail);
        existing.setFullName(updated.getFullName().trim());
        existing.setLocation(updated.getLocation().trim());
        existing.setAddress(updated.getAddress().trim());
        existing.setPhone(updated.getPhone().trim());
        existing.setAge(updated.getAge());

        try {
            userService.updateProfile(existing);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("user", existing);
            model.addAttribute("error", "That email address is already in use.");
            return "edit-account";
        }

        return "redirect:/profile?updated=true";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
