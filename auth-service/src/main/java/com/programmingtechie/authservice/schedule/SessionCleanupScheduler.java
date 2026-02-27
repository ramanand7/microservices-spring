package com.programmingtechie.authservice.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.programmingtechie.authservice.service.SessionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredSessions() {
        log.info("Starting session cleanup task");
        sessionService.cleanupExpiredSessions();
        log.info("Session cleanup task completed");
    }
}