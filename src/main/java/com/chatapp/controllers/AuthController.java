package com.chatapp.controllers;

import com.chatapp.models.UserAccount;
import com.chatapp.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints for user registration and login.
 * Frontend (HTML/JS) will call these to manage accounts.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserAccount account) {
        Map<String, Object> body = new HashMap<>();

        UserService.RegistrationResult result = userService.register(account);
        if (result == UserService.RegistrationResult.SUCCESS) {
            logger.info("Registered user via API: {}", account.getUsername());
            body.put("success", true);
            body.put("message", "Registration successful");
            return ResponseEntity.ok(body);
        } else if (result == UserService.RegistrationResult.USERNAME_EXISTS) {
            body.put("success", false);
            body.put("message", "Username already exists. Please choose a different username.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        } else {
            body.put("success", false);
            body.put("message", "Invalid username or password. Username and password cannot be empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserAccount account) {
        Map<String, Object> body = new HashMap<>();

        boolean ok = userService.login(account);
        if (ok) {
            logger.info("Login successful for {}", account.getUsername());
            body.put("success", true);
            body.put("message", "Login successful");
            return ResponseEntity.ok(body);
        } else {
            body.put("success", false);
            body.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }
}
