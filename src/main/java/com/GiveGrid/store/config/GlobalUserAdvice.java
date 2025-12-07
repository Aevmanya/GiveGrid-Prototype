package com.GiveGrid.store.config;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalUserAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addUserToModel(Authentication auth, Model model) {

        if (auth != null) {
            User user = userService.findByUsername(auth.getName());
            model.addAttribute("user", user);
        }
    }
}
