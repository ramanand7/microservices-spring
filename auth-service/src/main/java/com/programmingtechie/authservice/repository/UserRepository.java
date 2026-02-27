package com.programmingtechie.authservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.programmingtechie.authservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByUsernameOrMobileNumber(String username, String mobileNumber);

    boolean existsByUsername(String username);

    boolean existsByMobileNumber(String mobileNumber);

    List<User> findByRoleId(Long roleId);

    List<User> findByRoleName(String roleName);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.isLocked = false AND u.isActive = true")
    List<User> findActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.roleName = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);
}
