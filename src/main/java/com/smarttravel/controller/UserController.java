package com.smarttravel.controller;

import com.smarttravel.model.User;
import com.smarttravel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already registered");
            }
            com.smarttravel.model.User saved = userRepository.save(user);
            // don't send password back to client
            saved.setPassword(null);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // avoid leaking internal stack trace to client
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Optional<User> found = userRepository.findByEmail(user.getEmail());
            if (found.isPresent() && found.get().getPassword().equals(user.getPassword())) {
                com.smarttravel.model.User u = found.get();
                u.setPassword(null);
                return ResponseEntity.ok(u);
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> { u.setPassword(null); return ResponseEntity.ok(u); })
                .orElse(ResponseEntity.notFound().build());
    }
}
