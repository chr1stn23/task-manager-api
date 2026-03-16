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
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateValidJwtToken() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        String email = "christian@task.com";
        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should return true when token is valid and email matches")
    void shouldReturnTrueWhenTokenValid() {
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        boolean isValid = jwtService.isTokenValid(token, email);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false when email doesn't match")
    void shouldReturnFalseWhenEmailDoesNotMatch() {
        String token = jwtService.generateToken("correct@test.com");

        boolean isValid = jwtService.isTokenValid(token, "wrong@test.com");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException when token is expired")
    void shouldThrowExpiredJwtExceptionWhenExpired() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("old@test.com")
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(key)
                .compact();
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(expiredToken));
    }

    @Test
    @DisplayName("Should return false when email matches but token is expired")
    void shouldReturnFalseWhenEmailMatchesButExpired() {
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        JwtService spyService = spy(jwtService);
        doReturn(true).when(spyService).isTokenExpired(token);

        boolean result = spyService.isTokenValid(token, email);

        assertFalse(result);
    }
}
