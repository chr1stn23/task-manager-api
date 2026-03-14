package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.response.SessionResponseDTO;
import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.security.CurrentUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Nested
    @DisplayName("getSessions")
    class GetSessionsTests {
        @Test
        @DisplayName("Should return sessions from an user")
        void shouldReturnSessionsFromAnUser() {
            // Arrange
            Long userId = 1L;
            User user = User.builder().id(userId).build();
            String ipAddress1 = "200.48.11.50";
            String ipAddress2 = "200.48.11.15";
            LocalDateTime now = LocalDateTime.now();

            List<RefreshToken> refreshTokens = List.of(
                    RefreshToken.builder()
                            .id(10L)
                            .token("token 1")
                            .user(user)
                            .deviceName("Windows PC")
                            .userAgent("Windows")
                            .ipAddress(ipAddress1)
                            .createdAt(now.minusDays(2))
                            .build(),
                    RefreshToken.builder()
                            .id(11L)
                            .token("token 2")
                            .user(user)
                            .deviceName("Android Device")
                            .userAgent("Android")
                            .ipAddress(ipAddress2)
                            .createdAt(now.minusMinutes(30))
                            .build()
            );

            when(refreshTokenRepository.findActiveSessionsByUserId(eq(userId), any(Instant.class))).thenReturn(refreshTokens);

            // Act
            List<SessionResponseDTO> sessions = sessionService.getSessions(userId, "token 1");

            // Assert
            assertNotNull(sessions);
            assertEquals(2, sessions.size());

            SessionResponseDTO session1 = sessions.getFirst();
            assertEquals(10L, session1.id());
            assertEquals("Windows PC", session1.deviceName());
            assertEquals(ipAddress1, session1.ipAddress());
            assertEquals("Windows", session1.agentName());
            assertEquals(now.minusDays(2), session1.createdAt());
            assertTrue(session1.current());

            SessionResponseDTO session2 = sessions.get(1);
            assertEquals("Android Device", session2.deviceName());
            assertFalse(session2.current());

            verify(refreshTokenRepository, times(1)).findActiveSessionsByUserId(eq(userId), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("revokeSession")
    class RevokeSessionTests {
        @Test
        @DisplayName("Should revoke session when user is owner and session exists")
        void shouldRevokeSessionWhenUserIsOwnerAndSessionExists() {
            // Arrange
            Long userId = 3L;
            User user = User.builder().id(userId).build();
            Long sessionId = 21L;
            RefreshToken refreshToken = RefreshToken.builder().id(sessionId).user(user).revoked(false).build();
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.of(refreshToken));

            // Act
            sessionService.revokeSession(sessionId);

            // Assert
            assertTrue(refreshToken.isRevoked());
            verify(refreshTokenRepository).findById(sessionId);
        }

        @Test
        @DisplayName("Should throw not found exception when session does not exist")
        void shouldThrowNotFoundExceptionWhenSessionDoesNotExist() {
            // Arrange
            Long currentUserId = 2L;
            Long sessionId = 999L;
            when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> sessionService.revokeSession(sessionId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Session not found");
        }

        @Test
        @DisplayName("Should throw not found exception when user is not owner")
        void shouldThrowNotFoundExceptionWhenUserIsNotOwner() {
            // Arrange
            Long sessionId = 21L;
            Long currentUserId = 1L;
            Long ownerId = 5L;
            User owner = User.builder().id(ownerId).build();
            RefreshToken token = RefreshToken.builder().id(sessionId).user(owner).revoked(false).build();

            when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.of(token));

            // Act/Assert
            assertThatThrownBy(() -> sessionService.revokeSession(sessionId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Session not found");
            assertFalse(token.isRevoked());
        }
    }

    @Nested
    @DisplayName("adminRevokeSession")
    class AdminRevokeSessionTests {
        @Test
        @DisplayName("Should revoke session when admin provides correct userId and sessionId exists")
        void adminShouldRevokeSessionWhenOwnerMatches() {
            // Arrange
            Long userId = 10L;
            Long sessionId = 50L;
            User owner = User.builder().id(userId).build();
            RefreshToken token = RefreshToken.builder()
                    .id(sessionId)
                    .user(owner)
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.of(token));

            // Act
            sessionService.adminRevokeSession(userId, sessionId);

            // Assert
            assertTrue(token.isRevoked());
            verify(refreshTokenRepository).findById(sessionId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when admin tries to revoke non-existent session")
        void adminShouldThrowExceptionWhenSessionNotFound() {
            // Arrange
            Long userId = 10L;
            Long sessionId = 999L;
            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> sessionService.adminRevokeSession(userId, sessionId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Session not found");
        }

        @Test
        @DisplayName("Should throw NotFoundException when admin provides userId that doesn't match session owner")
        void adminShouldThrowExceptionWhenUserIdDoesNotMatchOwner() {
            // Arrange
            Long providedUserId = 10L;
            Long realOwnerId = 20L;
            Long sessionId = 50L;

            User realOwner = User.builder().id(realOwnerId).build();
            RefreshToken token = RefreshToken.builder()
                    .id(sessionId)
                    .user(realOwner)
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findById(sessionId)).thenReturn(Optional.of(token));

            // Act/Assert
            assertThatThrownBy(() -> sessionService.adminRevokeSession(providedUserId, sessionId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Session not found");
            assertFalse(token.isRevoked());
        }
    }

    @Nested
    @DisplayName("revokeAllSessions")
    class RevokeAllSessionsTests {
        @Test
        @DisplayName("Should call repository to revoke all sessions for a given user")
        void shouldRevokeAllSessions() {
            // Arrange
            Long userId = 1L;

            // Act
            sessionService.revokeAllSessions(userId);

            // Assert
            verify(refreshTokenRepository, times(1)).revokeAllSessionsByUserId(userId);
            verifyNoMoreInteractions(refreshTokenRepository);
        }
    }
}
