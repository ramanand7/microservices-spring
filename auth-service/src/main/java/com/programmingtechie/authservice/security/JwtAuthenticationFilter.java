package com.programmingtechie.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.programmingtechie.authservice.service.CustomUserDetailsService;
import com.programmingtechie.authservice.service.JwtService;
import com.programmingtechie.authservice.service.SessionService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;  // ← add this (or autowire it)
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (jwtService.isTokenExpired(jwt)) {
                sendCustomErrorResponse(response, "Access token has expired. Please renew it.", "Token Expired");
                return;
            }

            final String username = jwtService.extractUsername(jwt);
            if (username == null) {
                sendCustomErrorResponse(response, "Invalid or malformed token", "Invalid Token");
                return;
            }

            if (!jwtService.isAccessToken(jwt)) {
                sendCustomErrorResponse(response, "Invalid token type - access token required", "Invalid Token Type");
                return;
            }

            if (sessionService.findByAccessToken(jwt).isEmpty()) {
                sendCustomErrorResponse(response, "No active session found for this token", "Invalid Session");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isValidToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    sendCustomErrorResponse(response, "Token validation failed", "Invalid Token");
                    return;
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            sendCustomErrorResponse(response, "Access token has expired. Please renew it.", "Token Expired");
            return;
        } catch (io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.SignatureException e) {
            sendCustomErrorResponse(response, "Invalid or tampered token", "Invalid Token");
            return;
        } catch (Exception e) {
            log.error("Unexpected authentication error", e);
            sendCustomErrorResponse(response, "Authentication failed: " + e.getMessage(), "Authentication Failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Helper method (reuse it)
    private void sendCustomErrorResponse(HttpServletResponse response, String message, String errorType) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("errorType", errorType);
        body.put("status", 401);
        body.put("timestamp", LocalDateTime.now());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return "/auth/refresh".equals(path);
        // or: return path.startsWith("/auth/refresh") || path.equals("/auth/login") etc.
    }
}