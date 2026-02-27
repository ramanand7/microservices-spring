package com.programmingtechie.authservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.programmingtechie.authservice.dto.ApiResponse;
import com.programmingtechie.authservice.dto.UserResponse;
import com.programmingtechie.authservice.service.CustomUserDetailsService;
import com.programmingtechie.authservice.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            ApiResponse<UserResponse> response = userService.getUserProfile(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get user profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/user/profile"));
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<String>> getUserDashboard(Authentication authentication) {
        try {
            String username = authentication.getName();
            String message = "Welcome to your dashboard, " + username + "!";
            CustomUserDetailsService.CustomUserDetails userDetails = (CustomUserDetailsService.CustomUserDetails) authentication
                    .getPrincipal();
            log.info("User {} accessed the dashboard", userDetails.getMobileNumber());
            return ResponseEntity.ok(ApiResponse.success(message, "Dashboard accessed successfully"));
        } catch (Exception e) {
            log.error("Failed to access dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/user/dashboard"));
        }
    }

    
    @GetMapping("/userinfovalidate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserInfoDetails>> getUserInfoValidate(Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetailsService.CustomUserDetails userDetails) {
                UserInfoDetails details = new UserInfoDetails(
                        userDetails.getUsername(),
                        userDetails.getMobileNumber(),
                        userDetails.getAuthorities(),
                        userDetails.isAccountNonExpired(),
                        userDetails.isAccountNonLocked(),
                        userDetails.isCredentialsNonExpired(),
                        userDetails.isEnabled()
                );
                log.info("User {} accessed the dashboard", userDetails.getMobileNumber());
                log.info("User Details: {}", details);
                return ResponseEntity.ok(ApiResponse.success(details, "User details retrieved successfully"));
            } else {
                log.error("Principal is not of type CustomUserDetails. Actual type: {}", principal.getClass().getName());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid user details", "/user/userinfovalidate"));
            }
        } catch (Exception e) {
            log.error("Failed to access dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/user/dashboard"));
        }
    }

    public static class UserInfoDetails {
        private String username;
        private String mobileNumber;
        private Object authorities;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;

        public UserInfoDetails(String username, String mobileNumber, Object authorities,
                              boolean accountNonExpired, boolean accountNonLocked,
                              boolean credentialsNonExpired, boolean enabled) {
            this.username = username;
            this.mobileNumber = mobileNumber;
            this.authorities = authorities;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.enabled = enabled;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public Object getAuthorities() { return authorities; }
        public void setAuthorities(Object authorities) { this.authorities = authorities; }
        public boolean isAccountNonExpired() { return accountNonExpired; }
        public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
        public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
        public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
        public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        @Override
        public String toString() {
            return "UserInfoDetails{" +
                    "username='" + username + '\'' +
                    ", mobileNumber='" + mobileNumber + '\'' +
                    ", authorities=" + authorities +
                    ", accountNonExpired=" + accountNonExpired +
                    ", accountNonLocked=" + accountNonLocked +
                    ", credentialsNonExpired=" + credentialsNonExpired +
                    ", enabled=" + enabled +
                    '}';
        }
    }


    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<String>> getUserSettings() {
        try {
            return ResponseEntity.ok(ApiResponse.success("User settings data", "Settings retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to get user settings: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "/user/settings"));
        }
    }
}