package com.programmingtechie.authservice.service;

import com.programmingtechie.authservice.dto.ApiResponse;
import com.programmingtechie.authservice.dto.SystemStatsResponse;
import com.programmingtechie.authservice.dto.UserResponse;
import com.programmingtechie.authservice.entity.User;
import com.programmingtechie.authservice.exceptions.UserNotFoundException;
import com.programmingtechie.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public ApiResponse<UserResponse> getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserResponse userResponse = convertToUserResponse(user);
        return ApiResponse.success(userResponse, "User profile retrieved successfully");
    }

    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(userResponses, "Users retrieved successfully");
    }

    public ApiResponse<List<UserResponse>> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName.toUpperCase());
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(userResponses, "Users by role retrieved successfully");
    }

    public ApiResponse<String> deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated: {}", user.getUsername());
        return ApiResponse.success("User deactivated successfully", "User deactivated");
    }

    public ApiResponse<String> activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("User activated: {}", user.getUsername());
        return ApiResponse.success("User activated successfully", "User activated");
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setMobileNumber(user.getMobileNumber());
        response.setRoleName(user.getRoleName());
        response.setIsActive(user.getIsActive());
        response.setIsLocked(user.getIsLocked());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public ApiResponse<String> lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsLocked(true);
        userRepository.save(user);

        log.info("User locked: {}", user.getUsername());
        return ApiResponse.success("User locked successfully", "User locked");
    }

    public ApiResponse<String> unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("User unlocked: {}", user.getUsername());
        return ApiResponse.success("User unlocked successfully", "User unlocked");
    }

    public ApiResponse<Object> getSystemStats() {
        Long totalUsers = userRepository.count();
        Long activeUsers = (long) userRepository.findByIsActive(true).size();
        Long lockedUsers = (long) userRepository.findAll().stream()
                .mapToInt(user -> user.getIsLocked() ? 1 : 0)
                .sum();
        Long adminUsers = userRepository.countByRoleName("ADMIN");
        Long moderatorUsers = userRepository.countByRoleName("MODERATOR");
        Long regularUsers = userRepository.countByRoleName("USER");
        // Note: You'll need to add session counting methods to SessionRepository
        Long activeSessions = 0L; // Implement based on your needs
        Long totalSessions = 0L; // Implement based on your needs

        SystemStatsResponse stats = new SystemStatsResponse(
                totalUsers, activeUsers, lockedUsers, adminUsers,
                moderatorUsers, regularUsers, activeSessions, totalSessions);

        return ApiResponse.success(stats, "System statistics retrieved successfully");
    }
}