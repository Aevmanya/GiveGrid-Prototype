package com.GiveGrid.store.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Keeps the old /products/add bookmark working while the request-creation
 * endpoint uses the dedicated /products/create route.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductCreateRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("GET".equalsIgnoreCase(request.getMethod())
                && "/products/add".equals(request.getRequestURI())) {
            response.sendRedirect(request.getContextPath() + "/products/create");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
