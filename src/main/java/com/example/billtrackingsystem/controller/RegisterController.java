package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String showRegistrationForm(Principal principal, Model model) {
        if (principal != null) {
            return "redirect:/";
        }
        model.addAttribute("username", "");
        model.addAttribute("recoveryQuestion", "");
        model.addAttribute("recoveryAnswer", "");
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(Principal principal,
                               @RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam String recoveryQuestion,
                               @RequestParam String recoveryAnswer,
                               @RequestParam(defaultValue = "en") String preferredLanguage,
                               Model model) {
        if (principal != null) {
            return "redirect:/";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username '" + username + "' is already taken.");
            model.addAttribute("username", username);
            model.addAttribute("recoveryQuestion", recoveryQuestion);
            model.addAttribute("recoveryAnswer", recoveryAnswer);
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("username", username);
            model.addAttribute("recoveryQuestion", recoveryQuestion);
            model.addAttribute("recoveryAnswer", recoveryAnswer);
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        // Using plain text to match NoOpPasswordEncoder.getInstance()
        newUser.setPassword(password);
        newUser.setRecoveryQuestion(recoveryQuestion);
        newUser.setRecoveryAnswer(recoveryAnswer);
        newUser.setPreferredLanguage(preferredLanguage);

        userRepository.save(newUser);

        return "redirect:/login?registerSuccess";
    }
}
