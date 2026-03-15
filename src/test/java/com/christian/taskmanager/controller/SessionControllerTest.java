package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.response.SessionResponseDTO;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.security.JwtAuthenticationFilter;
import com.christian.taskmanager.service.SessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SessionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Nested
    @DisplayName("getSessions")
    class GetSessionsTests {
        @Test
        @DisplayName("Should return sessions when exists")
        void shouldReturnSessionsWhenExists() throws Exception {
            // Arrange
            Long userId = 1L;
            SessionResponseDTO response = new SessionResponseDTO(
                    2L, "Windows PC", "127.0.0.1", "Windows",
                    LocalDateTime.now().minusDays(1), true);
            Cookie refreshCookie = new Cookie("refreshToken", "refresh token");
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(sessionService.getSessions(userId, "refresh token")).thenReturn(List.of(response));

            // Act/Assert
            mockMvc.perform(get("/api/sessions")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(2L))
                    .andExpect(jsonPath("$.data[0].current").value(true));
        }
    }

    @Nested
    @DisplayName("revokeAllSessions")
    class RevokeAllSessionsTests {
        @Test
        @DisplayName("Should revoke all sessions successfully")
        void shouldRevokeAllSessionsSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            when(currentUserService.getCurrentUserId()).thenReturn(userId);

            // Act/Assert
            mockMvc.perform(delete("/api/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("All sessions revoked successfully"));
            verify(sessionService).revokeAllSessions(userId);
        }
    }

    @Nested
    @DisplayName("revokeSession")
    class RevokeSessionTests {
        @Test
        @DisplayName("Should revoke specific session successfully")
        void shouldRevokeSessionSuccessfully() throws Exception {
            // Arrange
            Long sessionId = 2L;

            // Act/Assert
            mockMvc.perform(delete("/api/sessions" +
                            "/{id}", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Session revoked successfully"));
            verify(sessionService).revokeSession(sessionId);
        }

        @Test
        @DisplayName("Should return 404 when session not found")
        void shouldReturn404WhenSessionNotFound() throws Exception {
            // Arrange
            Long sessionId = 99L;
            org.mockito.Mockito.doThrow(new NotFoundException("Session not found"))
                    .when(sessionService).revokeSession(sessionId);

            // Act/Assert
            mockMvc.perform(delete("/api/sessions/{id}", sessionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }
}
