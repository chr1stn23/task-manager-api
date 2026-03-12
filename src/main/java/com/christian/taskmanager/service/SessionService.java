package com.christian.taskmanager.service;

import com.christian.taskmanager.dto.response.SessionResponseDTO;

import java.util.List;

public interface SessionService {

    List<SessionResponseDTO> getSessions(Long userId, String currentRefreshToken);

    void revokeSession(Long sessionId);

    void adminRevokeSession(Long userId, Long sessionId);

    void revokeAllSessions(Long userId);
}
