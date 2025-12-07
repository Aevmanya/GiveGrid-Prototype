package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Login loads user by USERNAME
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }

    // Signup save method (✔ DO NOT ENCODE HERE)
    public User save(User user) {

        // password is already encoded in AuthController — DO NOT encode again

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("BUYER");
        }

        return userRepo.save(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return userRepo.findByUsername(username) != null;
    }

    public boolean emailExists(String email) {
        return userRepo.findByEmail(email) != null;
    }

    // Profile updates — no password encoding
    public User updateProfile(User user) {
        return userRepo.save(user);
    }

    // Separate safe password update
    public void updatePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }
}

