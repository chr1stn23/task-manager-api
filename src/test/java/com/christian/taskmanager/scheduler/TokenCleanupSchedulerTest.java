package com.christian.taskmanager.scheduler;

import com.christian.taskmanager.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Test
    @DisplayName("Should call deleteExpiredTokens with current time")
    void shouldCallDeleteExpiredTokens() {
        // Act
        tokenCleanupScheduler.cleanExpiredTokens();

        // Assert
        verify(refreshTokenRepository).deleteExpiredTokens(any(Instant.class));
    }
}