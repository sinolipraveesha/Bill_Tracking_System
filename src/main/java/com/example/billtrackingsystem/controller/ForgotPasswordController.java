package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Username not found.");
            return "forgot-password";
        }

        User user = userOpt.get();
        if (user.getRecoveryQuestion() == null || user.getRecoveryQuestion().isBlank()) {
            model.addAttribute("error", "No recovery security question has been set for this user. Please contact an administrator.");
            return "forgot-password";
        }

        model.addAttribute("username", username);
        model.addAttribute("question", user.getRecoveryQuestion());
        model.addAttribute("step", 2);
        return "forgot-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String username,
                                      @RequestParam String answer,
                                      @RequestParam String newPassword,
                                      Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "forgot-password";
        }

        User user = userOpt.get();
        String savedAnswer = user.getRecoveryAnswer();

        if (savedAnswer != null && savedAnswer.trim().equalsIgnoreCase(answer.trim())) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("username", username);
            model.addAttribute("question", user.getRecoveryQuestion());
            model.addAttribute("step", 2);
            model.addAttribute("error", "Incorrect recovery answer. Please try again.");
            return "forgot-password";
        }
    }
}
