package com.GiveGrid.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomLoginSuccessHandler successHandler;

    public SecurityConfig(UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          CustomLoginSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authProvider())

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC PAGES
                        .requestMatchers("/login", "/signup",
                                "/css/**", "/js/**", "/images/**").permitAll()

                        // PUBLIC browsing
                        .requestMatchers("/", "/products/*", "/search").permitAll()

                        // SELLER ONLY
                        .requestMatchers("/products", "/products/add",
                                "/products/edit/**", "/products/delete/**", "/seller/**")
                        .hasRole("SELLER")

                        // BUYER ONLY
                        .requestMatchers("/cart/**", "/checkout/**")
                        .hasRole("BUYER")

                        // EVERYTHING ELSE REQUIRES LOGIN
                        .anyRequest().authenticated()
                )

                // LOGIN CONFIG
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)   // <-- REDIRECT BUYER/SELLER
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // REMEMBER-ME
                .rememberMe(remember -> remember
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                        .key("SUPER_SECRET_KEY_CHANGE_ME")
                        .userDetailsService(userDetailsService)
                )

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        return http.build();
    }
}
