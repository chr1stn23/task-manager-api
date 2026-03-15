package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.security.JwtAuthenticationFilter;
import com.christian.taskmanager.service.AuthService;
import com.christian.taskmanager.service.model.TokenPair;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // Helpers
    private TokenPair tokenPair() {
        return new TokenPair("access-token", "refresh-token");
    }

    private RegisterRequestDTO registerRequest() {
        return new RegisterRequestDTO("User", "user@test.com", "password");
    }

    private AuthRequestDTO loginRequest() {
        return new AuthRequestDTO("user@test.com", "password");
    }

    @Nested
    @DisplayName("register")
    class RegisterTest {
        static Stream<Arguments> invalidRegisterRequests() {
            return Stream.of(
                    Arguments.of(
                            "Name is null",
                            new RegisterRequestDTO(null, "user@test.com", "password")
                    ),
                    Arguments.of(
                            "Name is blank",
                            new RegisterRequestDTO(" ", "user@test.com", "password")
                    ),
                    Arguments.of(
                            "Email is null",
                            new RegisterRequestDTO("User", null, "password")
                    ),
                    Arguments.of(
                            "Email is blank",
                            new RegisterRequestDTO("User", " ", "password")
                    ),
                    Arguments.of(
                            "Email is invalid",
                            new RegisterRequestDTO("User", "user@", "password")
                    ),
                    Arguments.of(
                            "Password is null",
                            new RegisterRequestDTO("User", "user@test.com", null)
                    ),
                    Arguments.of(
                            "Password is blank",
                            new RegisterRequestDTO("User", "user@test.com", " ")
                    )
            );
        }

        @Test
        @DisplayName("Should register user and return tokens")
        void shouldRegisterUserAndReturnTokens() throws Exception {
            // Arrange
            RegisterRequestDTO request = registerRequest();
            when(authService.register(any(), any(), any()))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", "JUnit")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.data.token").value("access-token"));
            verify(authService).register(any(), eq("JUnit"), any());
        }

        @ParameterizedTest(name = "Invalid request case: {0}")
        @MethodSource("invalidRegisterRequests")
        @DisplayName("Should return 400 Bad Request when register request fields are invalid")
        void shouldReturnBadRequestWhenFieldsAreInvalid(@SuppressWarnings("unused") String caseName,
                RegisterRequestDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {
        static Stream<Arguments> invalidLoginRequests() {
            return Stream.of(
                    Arguments.of(
                            "Email is null",
                            new AuthRequestDTO(null, "password")
                    ),
                    Arguments.of(
                            "Email is blank",
                            new AuthRequestDTO(" ", "password")
                    ),
                    Arguments.of(
                            "Password is null",
                            new AuthRequestDTO("user@test.com", null)
                    ),
                    Arguments.of(
                            "Password is blank",
                            new AuthRequestDTO("user@test.com", " ")
                    )
            );
        }

        @Test
        @DisplayName("Should login user and return tokens")
        void shouldLoginUserAndReturnTokens() throws Exception {
            // Arrange
            AuthRequestDTO request = loginRequest();
            when(authService.login(any(), any(), any()))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", "JUnit")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.data.token").value("access-token"));
            verify(authService).login(any(), eq("JUnit"), any());
        }

        @ParameterizedTest(name = "Invalid request case: {0}")
        @MethodSource("invalidLoginRequests")
        @DisplayName("Should return 400 Bad Request when login request fields are invalid")
        void shouldReturnBadRequestWhenFieldsAreInvalid(@SuppressWarnings("unused") String caseName,
                AuthRequestDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {
        @Test
        @DisplayName("Should refresh token when valid cookie provided")
        void shouldRefreshTokenWhenValidCookieProvided() throws Exception {
            // Arrange
            when(authService.refresh(any(), any(), any()))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refreshToken", "refresh-token"))
                            .header("User-Agent", "JUnit"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.data.token").value("access-token"));
            verify(authService).refresh(eq("refresh-token"), eq("JUnit"), any());
        }

        @Test
        @DisplayName("Should refresh token even when cookie is missing")
        void shouldRefreshTokenEvenWhenCookieMissing() throws Exception {
            // Arrange
            when(authService.refresh(any(), any(), any()))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/refresh")
                            .header("User-Agent", "JUnit"))
                    .andExpect(status().isOk());
            verify(authService).refresh(isNull(), eq("JUnit"), any());
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {
        @Test
        @DisplayName("Should logout and delete refresh token cookie")
        void shouldLogoutAndDeleteRefreshTokenCookie() throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refreshToken", "refresh-token")))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.data").value("Logout successful"));

            verify(authService).logout("refresh-token");
        }

        @Test
        @DisplayName("Should logout even when cookie is missing")
        void shouldLogoutEvenWhenCookieMissing() throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());

            verify(authService).logout(null);
        }
    }

    @Nested
    @DisplayName("extractIpAddress")
    class ExtractIpAddressTests {
        @Test
        @DisplayName("Should extract IP from X-Forwarded-For header")
        void shouldExtractIpFromXForwardedFor() throws Exception {
            // Arrange
            when(authService.login(any(), any(), eq("192.168.1.1")))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "192.168.1.1, 10.0.0.1")
                            .content(objectMapper.writeValueAsString(loginRequest())))
                    .andExpect(status().isOk());
            verify(authService).login(any(), any(), eq("192.168.1.1"));
        }

        @Test
        @DisplayName("Should extract IP from X-Real-IP header if there is no X-Forwarded-For")
        void shouldExtractIpFromXRealIp() throws Exception {
            // Arrange
            when(authService.login(any(), any(), eq("203.0.113.5")))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Real-IP", "203.0.113.5")
                            .content(objectMapper.writeValueAsString(loginRequest())))
                    .andExpect(status().isOk());

            verify(authService).login(any(), any(), eq("203.0.113.5"));
        }

        @Test
        @DisplayName("Should fallback to remoteAddr if header is unknown")
        void shouldFallbackToRemoteAddrIfHeaderIsUnknown() throws Exception {
            // Arrange
            when(authService.login(any(), any(), any()))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "unknown")
                            .content(objectMapper.writeValueAsString(loginRequest())))
                    .andExpect(status().isOk());
            verify(authService).login(any(), any(), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Should skip X-Forwarded-For if it is blank and move to next option")
        void shouldSkipXForwardedForIfBlank() throws Exception {
            // Arrange
            when(authService.login(any(), any(), eq("127.0.0.1")))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "   ")
                            .content(objectMapper.writeValueAsString(loginRequest())))
                    .andExpect(status().isOk());

            verify(authService).login(any(), any(), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Should skip X-Real-IP if it is blank")
        void shouldSkipXRealIpIfBlank() throws Exception {
            // Arrange
            when(authService.login(any(), any(), eq("127.0.0.1")))
                    .thenReturn(tokenPair());

            // Act/Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Real-IP", "")
                            .content(objectMapper.writeValueAsString(loginRequest())))
                    .andExpect(status().isOk());

            verify(authService).login(any(), any(), eq("127.0.0.1"));
        }
    }
}
