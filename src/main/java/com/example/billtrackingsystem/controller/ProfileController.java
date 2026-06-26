package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String currentUsername = principal.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Principal principal,
                                 @RequestParam String username,
                                 @RequestParam(required = false) String password,
                                 @RequestParam String recoveryQuestion,
                                 @RequestParam String recoveryAnswer,
                                 @RequestParam String preferredLanguage,
                                 Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String currentUsername = principal.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        // If username changes, check if it's already taken
        if (!username.equals(currentUsername) && userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Username '" + username + "' is already taken.");
            return "profile";
        }

        user.setUsername(username);
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        }
        user.setRecoveryQuestion(recoveryQuestion);
        user.setRecoveryAnswer(recoveryAnswer);
        user.setPreferredLanguage(preferredLanguage);
        userRepository.save(user);

        // Dynamically update authentication token in security context
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = new ArrayList<>(currentAuth.getAuthorities());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                user.getPassword(),
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/profile?success";
    }
}
