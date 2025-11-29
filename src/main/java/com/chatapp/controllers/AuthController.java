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

        boolean ok = userService.register(account);
        if (ok) {
            logger.info("Registered user via API: {}", account.getUsername());
            body.put("success", true);
            body.put("message", "Registration successful");
            return ResponseEntity.ok(body);
        } else {
            body.put("success", false);
            body.put("message", "Username invalid or already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
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


