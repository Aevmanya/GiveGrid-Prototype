package com.GiveGrid.store.config;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public CustomLoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        User user = userService.findByUsername(authentication.getName());

        if (user != null && !isProfileComplete(user)) {
            response.sendRedirect("/account/edit?completeProfile=true");
            return;
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ROLE_SELLER")) {
                response.sendRedirect("/seller/dashboard");
                return;
            }

            if (auth.getAuthority().equals("ROLE_BUYER")) {
                response.sendRedirect("/");
                return;
            }
        }

        response.sendRedirect("/");
    }

    private boolean isProfileComplete(User user) {
        return hasText(user.getFullName())
                && user.getAge() != null
                && hasText(user.getPhone())
                && hasText(user.getAddress())
                && hasText(user.getLocation());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
