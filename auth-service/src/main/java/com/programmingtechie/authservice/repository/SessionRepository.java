package com.programmingtechie.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.programmingtechie.authservice.entity.Session;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByAccessToken(String accessToken);

    Optional<Session> findByRefreshToken(String refreshToken);

    List<Session> findByUserId(Long userId);

    List<Session> findByMobileNumber(String mobileNumber);

    List<Session> findByUserIdAndSessionStatus(Long userId, String sessionStatus);

    @Query("SELECT s FROM Session s WHERE s.userId = :userId AND s.isActive = true AND s.sessionStatus = 'ACTIVE'")
    List<Session> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Session s WHERE s.refreshTokenExpiresAt < :now")
    List<Session> findExpiredRefreshTokenSessions(@Param("now") LocalDateTime now);

    void deleteByUserIdAndSessionStatus(Long userId, String sessionStatus);
}