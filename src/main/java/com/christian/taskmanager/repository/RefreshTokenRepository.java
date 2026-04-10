package com.christian.taskmanager.repository;

import com.christian.taskmanager.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(String token);

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findById(Long id);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.token = :token AND r.revoked = false")
    int revokeTokenIfActive(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now ")
    void deleteExpiredTokens(Instant now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findActiveSessionsByUserId(Long userId, Instant now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllSessionsByUserId(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :refreshToken AND rt.revoked = false")
    int revokeByToken(String refreshToken);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.revoked = false AND rt.expiryDate > :now")
    long countActiveSessions(@Param("now") Instant now);
}
