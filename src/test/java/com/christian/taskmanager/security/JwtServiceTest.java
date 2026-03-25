package com.christian.taskmanager.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class JwtServiceTest {

    private final String SECRET_KEY = "c0200a33f711ff47621f4872107b3a2e4cb14ca824b3b0accc6f35ab376036d1";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 900000L);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateValidJwtToken() {
        // Arrange/Act
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Arrange/Act
        String email = "christian@task.com";
        String token = jwtService.generateToken(email);
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should return true when token is valid and email matches")
    void shouldReturnTrueWhenTokenValid() {
        // Arrange/Act
        String email = "user@test.com";
        String token = jwtService.generateToken(email);
        boolean isValid = jwtService.isTokenValid(token, email);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false when email doesn't match")
    void shouldReturnFalseWhenEmailDoesNotMatch() {
        // Arrange/Act
        String token = jwtService.generateToken("correct@test.com");
        boolean isValid = jwtService.isTokenValid(token, "wrong@test.com");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException when token is expired")
    void shouldThrowExpiredJwtExceptionWhenExpired() {
        // Arrange
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("old@test.com")
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(key)
                .compact();

        // Act/Assert
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(expiredToken));
    }

    @Test
    @DisplayName("Should return false when email matches but token is expired")
    void shouldReturnFalseWhenEmailMatchesButExpired() {
        // Arrange/Act
        String email = "user@test.com";
        String token = jwtService.generateToken(email);
        JwtService spyService = spy(jwtService);
        doReturn(true).when(spyService).isTokenExpired(token);

        boolean result = spyService.isTokenValid(token, email);

        // Assert
        assertFalse(result);
    }
}
