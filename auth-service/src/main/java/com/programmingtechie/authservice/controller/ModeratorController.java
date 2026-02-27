package com.programmingtechie.authservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.programmingtechie.authservice.dto.ApiResponse;
import com.programmingtechie.authservice.dto.UserResponse;
import com.programmingtechie.authservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/moderator")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ModeratorController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        try {
            ApiResponse<List<UserResponse>> response = userService.getUsersByRole("USER");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get users: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/moderator/users"));
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getModeratorDashboard(Authentication authentication) {
        try {
            String username = authentication.getName();
            String message = "Welcome to moderator dashboard, " + username + "!";
            return ResponseEntity.ok(ApiResponse.success(message, "Moderator dashboard accessed successfully"));
        } catch (Exception e) {
            log.error("Failed to access moderator dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/moderator/dashboard"));
        }
    }

    @PutMapping("/users/{userId}/lock")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> lockUser(@PathVariable Long userId) {
        try {
            ApiResponse<String> response = userService.lockUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to lock user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/moderator/users/" + userId + "/lock"));
        }
    }

    @PutMapping("/users/{userId}/unlock")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> unlockUser(@PathVariable Long userId) {
        try {
            ApiResponse<String> response = userService.unlockUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to unlock user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/moderator/users/" + userId + "/unlock"));
        }
    }
}