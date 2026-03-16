package com.christian.taskmanager.security;

import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static Stream<Arguments> provideUserAgents() {
        return Stream.of(
                Arguments.of("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)", "iOS Device"),
                Arguments.of("iPad", "Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)", "iOS Device"),
                Arguments.of("Android", "Mozilla/5.0 (Linux; Android 10; SM-G973F)", "Android Device"),
                Arguments.of("Windows", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Windows PC"),
                Arguments.of("iMac", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", "MacBook/iMac"),
                Arguments.of("Linux", "Mozilla/5.0 (X11; Linux x86_64)", "Linux Device"),
                Arguments.of("Custom", "MyCustomBrowser/1.0", "Unknown Device"),
                Arguments.of("Unknown", null, "Unknown")
        );
    }

    @Test
    @DisplayName("Should create refresh token with correct device name")
    void shouldCreateRefreshTokenWithCorrectDeviceName() {
        // Arrange
        Long userId = 1L;
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(userId, userAgent, "127.0.0.1");

        // Assert
        assertNotNull(result);
        assertEquals("Windows PC", result.getDeviceName());
        assertEquals(user, result.getUser());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist during token creation")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act/Assert
        assertThrows(NotFoundException.class,
                () -> refreshTokenService.createRefreshToken(userId, "agent", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should throw exception when verifying a revoked token")
    void shouldThrowExceptionWhenTokenRevoked() {
        // Arrange
        RefreshToken token = RefreshToken.builder().revoked(true).build();

        // Act/Assert
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token revoked");
    }

    @Test
    @DisplayName("Should delete and throw exception when token is expired")
    void shouldThrowExceptionWhenTokenExpired() {
        // Arrange
        RefreshToken token = RefreshToken.builder()
                .revoked(false)
                .expiryDate(Instant.now().minusSeconds(60)) // Expired 1 minute ago
                .build();

        // Act/Assert
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token expired");
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    @DisplayName("Should throw Exception when token is not found")
    void shouldThrowExceptionWhenTokenNotFound() {
        // Arrange
        String tokenStr = "uuid-token";
        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        // Act/Assert
        assertThatThrownBy(() -> refreshTokenService.findByToken(tokenStr))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Refresh token not found");
    }

    @ParameterizedTest(name = "User-Agent case: {0}")
    @MethodSource("provideUserAgents")
    @DisplayName("Should parse various User-Agents to correct device names")
    void shouldParseUserAgentToCorrectDeviceName(@SuppressWarnings("unused") String caseName, String userAgent,
            String expectedDevice) {
        // Arrange
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(userId, userAgent, "127.0.0.1");

        // Assert
        assertEquals(expectedDevice, result.getDeviceName());
    }

    @Test
    @DisplayName("Should do nothing when token is valid (not revoked and not expired)")
    void shouldDoNothingWhenTokenValid() {
        // Arrange
        RefreshToken token = RefreshToken.builder()
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(3600)) // Expire in 1 hour
                .build();

        // Act/Assert
        assertDoesNotThrow(() -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should return token when it exists")
    void shouldReturnTokenWhenFound() {
        // Arrange
        String tokenStr = "uuid-token";
        RefreshToken expected = RefreshToken.builder()
                .token(tokenStr)
                .build();
        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(expected));

        // Act
        RefreshToken result = refreshTokenService.findByToken(tokenStr);

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
        verify(refreshTokenRepository).findByToken(tokenStr);
    }
}
