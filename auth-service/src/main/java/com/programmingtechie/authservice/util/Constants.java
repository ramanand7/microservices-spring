package com.programmingtechie.authservice.util;

public class Constants {

    // Role Constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_MODERATOR = "MODERATOR";

    // Session Status Constants
    public static final String SESSION_ACTIVE = "ACTIVE";
    public static final String SESSION_LOGGED_OUT = "LOGGED_OUT";
    public static final String SESSION_EXPIRED = "EXPIRED";
    public static final String SESSION_REPLACED = "REPLACED";

    // Token Type Constants
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    // API Response Messages
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String REGISTRATION_SUCCESS = "User registered successfully";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully";

    // Error Messages
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String ACCOUNT_LOCKED = "Account is locked due to multiple failed login attempts";
    public static final String ACCOUNT_DEACTIVATED = "Account is deactivated";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String TOKEN_EXPIRED = "Token has expired. Please login again.";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}