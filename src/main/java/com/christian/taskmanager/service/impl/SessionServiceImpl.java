package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.response.SessionResponseDTO;
import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final CurrentUserService currentUserService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDTO> getSessions(Long userId, String currentRefreshToken) {
        List<RefreshToken> tokens = refreshTokenRepository.findActiveSessionsByUserId(userId, Instant.now());

        return tokens.stream()
                .map(token -> new SessionResponseDTO(
                        token.getId(),
                        token.getDeviceName(),
                        token.getIpAddress(),
                        token.getUserAgent(),
                        token.getCreatedAt(),
                        token.getToken().equals(currentRefreshToken)
                )).toList();
    }

    @Override
    @Transactional
    public void revokeSession(Long sessionId) {
        Long currentUserId = currentUserService.getCurrentUserId();

        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (!token.getUser().getId().equals(currentUserId)) {
            throw new NotFoundException("Session not found");
        }

        token.setRevoked(true);
    }

    @Override
    @Transactional
    public void adminRevokeSession(Long userId, Long sessionId) {
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (!token.getUser().getId().equals(userId)) {
            throw new NotFoundException("Session not found");
        }

        token.setRevoked(true);
    }

    @Override
    @Transactional
    public void revokeAllSessions(Long userId) {
        refreshTokenRepository.revokeAllSessionsByUserId(userId);
    }
}
