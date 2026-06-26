package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("preferredLanguage")
    public String getPreferredLanguage(Principal principal) {
        if (principal == null) {
            return "en";
        }
        return userRepository.findByUsername(principal.getName())
                .map(User::getPreferredLanguage)
                .orElse("en");
    }

    @ModelAttribute("currentUri")
    public String getCurrentUri(jakarta.servlet.http.HttpServletRequest request) {
        return request.getRequestURI();
    }
}
