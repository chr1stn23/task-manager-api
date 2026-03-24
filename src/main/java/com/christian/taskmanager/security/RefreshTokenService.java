package com.christian.taskmanager.security;

import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.exception.RefreshTokenException.RefreshTokenExpiredException;
import com.christian.taskmanager.exception.RefreshTokenException.RefreshTokenRevokedException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final Duration refreshTokenDuration = Duration.ofDays(7);

    public RefreshToken createRefreshToken(Long userId, String userAgent, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(refreshTokenDuration))
                .revoked(false)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .deviceName(parseDeviceName(userAgent))
                .build();

        return refreshTokenRepository.save(token);
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new RefreshTokenRevokedException(token.getToken());
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException(token.getToken());
        }
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
    }

    private String parseDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown";

        String uaLower = userAgent.toLowerCase();

        if (uaLower.contains("iphone") || uaLower.contains("ipad")) return "iOS Device";
        if (uaLower.contains("android")) return "Android Device";
        if (uaLower.contains("windows")) return "Windows PC";
        if (uaLower.contains("macintosh")) return "MacBook/iMac";
        if (uaLower.contains("linux")) return "Linux Device";

        return "Unknown Device";
    }
}
