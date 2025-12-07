package com.GiveGrid.store.config;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Check roles
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

        // Fallback
        response.sendRedirect("/");
    }
}

