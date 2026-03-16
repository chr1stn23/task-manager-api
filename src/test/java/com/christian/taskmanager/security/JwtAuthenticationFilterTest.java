package com.christian.taskmanager.security;

import com.christian.taskmanager.exception.ExpiredTokenException;
import com.christian.taskmanager.exception.InvalidTokenException;
import com.christian.taskmanager.exception.TokenProcessingException;
import com.christian.taskmanager.exception.UserDisabledException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtAuthenticationEntryPoint entryPoint;
    @Mock
    private FilterChain filterChain;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should continue filter chain if no Authorization header")
    void shouldContinueFilterChainIfNoAuthorizationHeader() throws Exception {
        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should authenticate if token is valid")
    void shouldAuthenticateIfValidToken() throws Exception {
        // Arrange
        String token = "valid.token.here";
        String email = "christian@test.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails.getUsername())).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should call entryPoint on Expired Token")
    void shouldCallEntryPointOnExpiredToken() throws Exception {
        // Arrange
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenThrow(
                new ExpiredJwtException(null, null, "Expired")
        );

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(entryPoint).commence(any(), any(), any(ExpiredTokenException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should call entryPoint on Disabled User")
    void shouldCallEntrypointOnDisabledUser() throws Exception {
        // Arrange
        String token = "valid.token";
        String email = "disabled@test.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenThrow(new DisabledException("disabled"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(entryPoint).commence(any(), any(), any(UserDisabledException.class));
    }

    @Test
    @DisplayName("Should call entryPoint on Invalid Signature (SignatureException)")
    void shouldCallEntrypointOnInvalidSignature() throws Exception {
        // Arrange
        String token = "invalid.sig.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenThrow(new SignatureException("Invalid sig"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(entryPoint).commence(any(), any(), any(InvalidTokenException.class));
    }

    @Test
    @DisplayName("Should call entryPoint on General Exception (TokenProcessingException)")
    void shouldCallEntrypointOnGeneralException() throws Exception {
        // Arrange
        String token = "some.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(entryPoint).commence(any(), any(), any(TokenProcessingException.class));
    }

    @Test
    @DisplayName("Should continue chain if header does not start with Bearer")
    void shouldContinueChainIfHeaderDoesNotStartWithBearer() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue chain if token is invalid (isTokenValid = false)")
    void shouldContinueChainIfTokenInvalid() throws Exception {
        // Arrange
        String token = "invalid.token";
        String email = "test@test.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(jwtService.isTokenValid(token, email)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue chain if email is null in token (False Hit: email != null)")
    void shouldContinueChainIfEmailIsNullInToken() throws Exception {
        // Arrange
        String token = "token.with.no.subject";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue chain if already authenticated (False Hit: getAuthentication == null)")
    void shouldContinueChainIfAlreadyAuthenticated() throws Exception {
        // Arrange
        String token = "any.token";
        String email = "existing@test.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractEmail(token)).thenReturn(email);

        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                "existingUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtService, never()).isTokenValid(any(), any());
    }
}
