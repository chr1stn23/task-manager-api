package com.christian.taskmanager.security;

import com.christian.taskmanager.exception.ExpiredTokenException;
import com.christian.taskmanager.exception.InvalidTokenException;
import com.christian.taskmanager.exception.TokenProcessingException;
import com.christian.taskmanager.exception.UserDisabledException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static Stream<Arguments> provideExceptions() {
        return Stream.of(
                Arguments.of("Token is invalid", new InvalidTokenException(), "Token is invalid", "INVALID_TOKEN"),
                Arguments.of("Token is expired", new ExpiredTokenException(), "session has expired", "EXPIRED_TOKEN"),
                Arguments.of("Token processing error", new TokenProcessingException(), "Error processing",
                        "TOKEN_PROCESSING_ERROR"),
                Arguments.of("User is disabled", new UserDisabledException(), "User is disabled", "USER_DISABLED"),
                Arguments.of("No token provided", new InsufficientAuthenticationException("No token"), "A valid " +
                                "token access",
                        "NO_TOKEN_PROVIDED")
        );
    }

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @ParameterizedTest(name = "Exception case: {0}")
    @MethodSource("provideExceptions")
    @DisplayName("Should return correct JSON and status when specific exceptions occur")
    void shouldReturnCorrectErrorResponseWhenExceptionOccurs(
            @SuppressWarnings("unused") String caseName,
            AuthenticationException exception,
            String expectedMessage,
            String expectedCode
    ) throws IOException {
        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String content = response.getContentAsString();
        assertTrue(content.contains(expectedMessage));
        assertTrue(content.contains(expectedCode));
    }

    @Test
    @DisplayName("Should return default Unauthorized when unknown exception occurs")
    void shouldReturnDefaultUnauthorizedWhenUnknownExceptionOccurs() throws IOException {
        // Arrange
        AuthenticationException genericEx = new AuthenticationException("Generic") {
        };

        // Act
        entryPoint.commence(request, response, genericEx);

        // Assert
        String content = response.getContentAsString();
        assertTrue(content.contains("Unauthorized."));
        assertTrue(content.contains("UNAUTHORIZED"));
    }
}
