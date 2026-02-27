package com.programmingtechie.authservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.programmingtechie.authservice.dto.ApiResponse;
import com.programmingtechie.authservice.dto.UserResponse;
import com.programmingtechie.authservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            ApiResponse<List<UserResponse>> response = userService.getAllUsers();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get all users: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/users"));
        }
    }

    @GetMapping("/users/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable String roleName) {
        try {
            ApiResponse<List<UserResponse>> response = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get users by role: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/users/role/" + roleName));
        }
    }

    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long userId) {
        try {
            ApiResponse<String> response = userService.deactivateUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to deactivate user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/users/" + userId + "/deactivate"));
        }
    }

    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
        try {
            ApiResponse<String> response = userService.activateUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to activate user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/users/" + userId + "/activate"));
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getAdminDashboard() {
        try {
            return ResponseEntity
                    .ok(ApiResponse.success("Admin Dashboard Data", "Admin dashboard accessed successfully"));
        } catch (Exception e) {
            log.error("Failed to access admin dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/dashboard"));
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getSystemStats() {
        try {
            ApiResponse<Object> response = userService.getSystemStats();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get system stats: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/admin/stats"));
        }
    }
}