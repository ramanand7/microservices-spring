package com.programmingtechie.authservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String mobileNumber;
    private String roleName;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}