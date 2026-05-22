package com.example.billtrackingsystem.config;

import com.example.billtrackingsystem.model.User;
import com.example.billtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize the admin user if no users are present in the database.
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            // Using plain text to match NoOpPasswordEncoder.getInstance()
            admin.setPassword("password123");
            admin.setRecoveryQuestion("What is your default recovery key?");
            admin.setRecoveryAnswer("admin123");

            userRepository.save(admin);
            System.out.println("--------------------------------------------------");
            System.out.println("Default Admin User Initialized successfully!");
            System.out.println("Username: admin");
            System.out.println("Password: password123");
            System.out.println("Recovery Question: What is your default recovery key?");
            System.out.println("Recovery Answer: admin123");
            System.out.println("Please change these default credentials immediately.");
            System.out.println("--------------------------------------------------");
        }
    }
}
