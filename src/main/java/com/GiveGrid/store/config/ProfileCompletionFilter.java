package com.GiveGrid.store.config;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ProfileCompletionFilter extends OncePerRequestFilter {

    private final UserService userService;

    public ProfileCompletionFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        String path = request.getRequestURI();

        if (auth != null
                && auth.isAuthenticated()
                && !isAllowedPath(path)
                && !isProfileComplete(userService.findByUsername(auth.getName()))) {
            response.sendRedirect(request.getContextPath() + "/account/edit?completeProfile=true");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return path.equals("/login")
                || path.startsWith("/login/")
                || path.equals("/signup")
                || path.startsWith("/signup/")
                || path.equals("/account/edit")
                || path.equals("/logout")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/");
    }

    private boolean isProfileComplete(User user) {
        return user != null
                && hasText(user.getFullName())
                && user.getAge() != null
                && hasText(user.getPhone())
                && hasText(user.getAddress())
                && hasText(user.getLocation());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
