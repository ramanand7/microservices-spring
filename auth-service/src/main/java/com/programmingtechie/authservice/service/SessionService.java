package com.programmingtechie.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.programmingtechie.authservice.entity.Session;
import com.programmingtechie.authservice.entity.User;
import com.programmingtechie.authservice.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;

    public Session createSession(User user, String accessToken, String refreshToken, HttpServletRequest request) {
        // Deactivate existing active sessions for the user
        List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(user.getId());
        activeSessions.forEach(session -> {
            session.setIsActive(false);
            session.setSessionStatus("REPLACED");
            sessionRepository.save(session);
        });

        // Create new session
        Session session = new Session();
        session.setUserId(user.getId());
        session.setMobileNumber(user.getMobileNumber());
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setLoginTime(LocalDateTime.now());
        session.setIpAddress(getClientIpAddress(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setDeviceInfo(extractDeviceInfo(request));
        session.setSessionStatus("ACTIVE");
        session.setTokenExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration() / 1000));
        session.setRefreshTokenExpiresAt(
                LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000));
        session.setIsActive(true);

        Session savedSession = sessionRepository.save(session);
        log.info("Session created for user: {}", user.getUsername());
        return savedSession;
    }

    public void deactivateSession(Session session, HttpServletRequest request) {
        session.setLogoutTime(LocalDateTime.now());
        session.setIsActive(false);
        session.setSessionStatus("LOGGED_OUT");
        sessionRepository.save(session);
        log.info("Session deactivated for user ID: {}", session.getUserId());
    }

    public void updateSession(Session session) {
        sessionRepository.save(session);
    }

    public Optional<Session> findByAccessToken(String accessToken) {
        return sessionRepository.findByAccessToken(accessToken);
    }

    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }

    public List<Session> getActiveSessionsByUserId(Long userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    public void cleanupExpiredSessions() {
        List<Session> expiredSessions = sessionRepository.findExpiredRefreshTokenSessions(LocalDateTime.now());
        expiredSessions.forEach(session -> {
            session.setIsActive(false);
            session.setSessionStatus("EXPIRED");
            sessionRepository.save(session);
        });
        log.info("Cleaned up {} expired sessions", expiredSessions.size());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null)
            return "Unknown";

        if (userAgent.contains("Mobile")) {
            return "Mobile Device";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}