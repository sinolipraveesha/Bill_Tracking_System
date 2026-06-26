package com.example.billtrackingsystem.controller;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/language")
    public ResponseEntity<Void> updateLanguage(@RequestParam String lang, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            user.setPreferredLanguage(lang);
            userRepository.save(user);
        });
        return ResponseEntity.ok().build();
    }
}
