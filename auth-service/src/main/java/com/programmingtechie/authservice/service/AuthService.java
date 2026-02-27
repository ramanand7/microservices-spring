package com.programmingtechie.authservice.service;

import com.programmingtechie.authservice.dto.*;
import com.programmingtechie.authservice.entity.Role;
import com.programmingtechie.authservice.entity.Session;
import com.programmingtechie.authservice.entity.User;
import com.programmingtechie.authservice.exceptions.InvalidTokenException;
import com.programmingtechie.authservice.exceptions.UserAlreadyExistsException;
import com.programmingtechie.authservice.exceptions.UserNotFoundException;
import com.programmingtechie.authservice.repository.RoleRepository;
import com.programmingtechie.authservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SessionService sessionService;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<UserResponse> register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());

        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new UserAlreadyExistsException("Mobile number already exists");
        }

        // Get role
        Role role = roleRepository.findByName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoleId(role.getId());
        user.setRoleName(role.getName());
        user.setIsActive(true);
        user.setIsLocked(false);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        UserResponse userResponse = convertToUserResponse(savedUser);
        return ApiResponse.success(userResponse, "User registered successfully");
    }

    public ApiResponse<TokenResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Attempting login for: {}", request.getUsernameOrMobile());

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrMobile(), request.getPassword()));
            // holds the username and passwerd provided by the user

            // Get user details
            User user = userRepository.findByUsernameOrMobileNumber(
                    request.getUsernameOrMobile(), request.getUsernameOrMobile())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Check if user account is active and not locked
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is deactivated");
            }

            if (user.getIsLocked()) {
                throw new RuntimeException("Account is locked due to multiple failed login attempts");
            }

            // Generate tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String accessToken = jwtService.generateAccessToken(userDetails, user.getMobileNumber(),
                    user.getRoleName(), user.getId());
            String refreshToken = jwtService.generateRefreshToken(userDetails, user.getMobileNumber());

            // Update user login info
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Create session
            sessionService.createSession(user, accessToken, refreshToken, httpRequest);

            // Prepare response
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(accessToken);
            tokenResponse.setRefreshToken(refreshToken);
            tokenResponse.setExpiresIn(jwtService.getAccessTokenExpiration());
            tokenResponse.setIssuedAt(LocalDateTime.now());
            tokenResponse.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration() / 1000));
            tokenResponse.setUser(convertToUserResponse(user));

            log.info("User logged in successfully: {}", user.getUsername());
            return ApiResponse.success(tokenResponse, "Login successful");

        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            userRepository.findByUsernameOrMobileNumber(request.getUsernameOrMobile(), request.getUsernameOrMobile())
                    .ifPresent(user -> {
                        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                        if (user.getFailedLoginAttempts() >= 50) {
                            user.setIsLocked(true);
                            log.warn("Account locked due to multiple failed attempts: {}", user.getUsername());
                        }
                        userRepository.save(user);
                    });

            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public ApiResponse<TokenResponse> refreshToken(String refreshToken) {
        log.info("Attempting to refresh token");

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token type");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidTokenException("Refresh token has expired. Please login again.");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate refresh token in session
        Session session = sessionService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!session.getIsActive() || !"ACTIVE".equals(session.getSessionStatus())) {
            throw new InvalidTokenException("Session is not active");
        }

        // Generate new access token
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtService.generateAccessToken(userDetails, user.getMobileNumber(), user.getRoleName(),
                user.getId());

        // Update session with new access token
        session.setAccessToken(newAccessToken);
        session.setTokenExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration() / 1000));
        sessionService.updateSession(session);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(newAccessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setExpiresIn(jwtService.getAccessTokenExpiration());
        tokenResponse.setIssuedAt(LocalDateTime.now());
        tokenResponse.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration() / 1000));
        tokenResponse.setUser(convertToUserResponse(user));

        log.info("Token refreshed successfully for user: {}", username);
        return ApiResponse.success(tokenResponse, "Token refreshed successfully");
    }

    public ApiResponse<String> logout(String accessToken, HttpServletRequest httpRequest) {
        log.info("Attempting logout");

        try {
            String username = jwtService.extractUsername(accessToken);

            // Find and deactivate session
            Session session = sessionService.findByAccessToken(accessToken)
                    .orElseThrow(() -> new InvalidTokenException("Invalid access token"));

            sessionService.deactivateSession(session, httpRequest);

            log.info("User logged out successfully: {}", username);
            return ApiResponse.success("Logged out successfully", "Logout successful");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
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
}