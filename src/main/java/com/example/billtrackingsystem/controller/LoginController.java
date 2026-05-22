package com.example.billtrackingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        // Points to src/main/resources/templates/login.html
        return "login";
    }

    @GetMapping("/")
    public String home() {
        // Points to src/main/resources/templates/index.html
        return "index";
    }
}