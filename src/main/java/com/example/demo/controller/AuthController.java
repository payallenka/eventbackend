package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String token = loginRequest.get("token");
            String email = loginRequest.get("email");
            
            if (token == null || email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token and email are required"));
            }

            // Check if user exists in our database
            Optional<User> existingUser = userService.findByEmail(email);
            
            if (existingUser.isPresent()) {
                // User exists, return user data
                User user = existingUser.get();
                return ResponseEntity.ok(Map.of(
                    "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "role", user.getRole(),
                        "picture", user.getPicture()
                    )
                ));
            } else {
                // User doesn't exist, return null to trigger registration
                return ResponseEntity.ok(Map.of("user", (Object) null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String email = registerRequest.get("email");
            String name = registerRequest.get("name");
            String picture = registerRequest.get("picture");
            String roleStr = registerRequest.get("role");
            
            if (email == null || name == null || roleStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email, name, and role are required"));
            }

            // Check if user already exists
            Optional<User> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
            }

            // Create new user
            User.Role role;
            try {
                role = User.Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }

            User user = userService.createUser(email, name, picture, role);

            return ResponseEntity.ok(Map.of(
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole(),
                    "picture", user.getPicture()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole(),
                "picture", user.getPicture()
            ));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/promote-admin")
    public ResponseEntity<?> promoteToAdmin(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser.getRole() == User.Role.ADMIN) {
                String email = request.get("email");
                try {
                    User promotedUser = userService.promoteToAdmin(email);
                    return ResponseEntity.ok(Map.of("message", "User promoted to admin", "user", promotedUser));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
            }
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only admins can promote users"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
            }

            String email = authentication.getName();
            String name = profileRequest.get("name");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }

            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();
            user.setName(name.trim());
            User updatedUser = userService.save(user);

            return ResponseEntity.ok(Map.of(
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "name", updatedUser.getName(),
                    "role", updatedUser.getRole(),
                    "picture", updatedUser.getPicture()
                ),
                "message", "Profile updated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
