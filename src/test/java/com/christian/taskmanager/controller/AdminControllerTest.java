package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.ResetPasswordByAdminDTO;
import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.security.JwtAuthenticationFilter;
import com.christian.taskmanager.service.SessionService;
import com.christian.taskmanager.service.TaskService;
import com.christian.taskmanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private SessionService sessionService;

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {
        static Stream<Arguments> invalidCreateRequests() {
            return Stream.of(
                    Arguments.of("Name is null",
                            new UserCreateDTO(null, "test@mail.com", "12345678", List.of(Role.ROLE_USER), true)),
                    Arguments.of("Name is blank",
                            new UserCreateDTO("   ", "test@mail.com", "12345678", List.of(Role.ROLE_USER), true)),

                    Arguments.of("Email is invalid",
                            new UserCreateDTO("User", "invalid-email", "12345678", List.of(Role.ROLE_USER), true)),

                    Arguments.of("Password too short (< 8)",
                            new UserCreateDTO("User", "test@mail.com", "1234567", List.of(Role.ROLE_USER), true)),

                    Arguments.of("Roles list empty",
                            new UserCreateDTO("User", "test@mail.com", "12345678", List.of(), true)),
                    Arguments.of("Roles list null",
                            new UserCreateDTO("User", "test@mail.com", "12345678", null, true))
            );
        }

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {
            // Arrange
            UserCreateDTO request = new UserCreateDTO(
                    "User", "user@test.com", "pA$$w0rd", List.of(Role.ROLE_USER),
                    true);

            UserResponseDTO response = new UserResponseDTO(
                    1L, "User", "user@test.com", List.of(Role.ROLE_USER.toString()), true);

            when(userService.create(any(UserCreateDTO.class))).thenReturn(response);

            // Act/Assert
            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("User"));
        }

        @ParameterizedTest(name = "Invalid request case: {0}")
        @MethodSource("invalidCreateRequests")
        @DisplayName("Should return 400 Bad Request when create request fields are invalid")
        void shouldReturnBadRequestWhenFieldsAreInvalid(@SuppressWarnings("unused") String caseName,
                UserCreateDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsersTests {
        @Test
        @DisplayName("Should return paginated users with filters")
        void shouldReturnPaginatedUsers() throws Exception {
            // Arrange
            UserListResponseDTO user = new UserListResponseDTO(1L, "Admin", "admin@test.com", true);
            Page<UserListResponseDTO> page = new PageImpl<>(List.of(user));
            when(userService.getUsers(eq("Admin"), isNull(), eq(true), any(Pageable.class)))
                    .thenReturn(page);

            // Act/Assert
            mockMvc.perform(get("/api/admin/users")
                            .param("name", "Admin")
                            .param("enabled", "true")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].email").value("admin@test.com"));
            verify(userService).getUsers(eq("Admin"), isNull(), eq(true), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return 400 when enabled parameter is invalid")
        void shouldReturn400WhenEnabledIsInvalid() throws Exception {
            // Act/Assert
            mockMvc.perform(get("/api/admin/users")
                            .param("enabled", "not_a_boolean"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.error.message").value("Invalid value for parameter: enabled"));
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {
        @Test
        @DisplayName("Should return user when ID exists")
        void shouldReturnUserWhenIdExists() throws Exception {
            // Arrange
            Long userId = 1L;
            UserResponseDTO response = new UserResponseDTO(
                    userId, "Mario", "test@test.com", List.of("ROLE_USER"), true
            );

            when(userService.getUserById(userId)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(get("/api/admin/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.name").value("Mario"))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"));
            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return 404 when user ID does not exist")
        void shouldReturn404WhenInvalidFields() throws Exception {
            // Arrange
            Long userId = 99L;
            String errorMessage = "User not found";

            when(userService.getUserById(userId))
                    .thenThrow(new NotFoundException(errorMessage));

            // Act/Assert
            mockMvc.perform(get("/api/admin/users/{id}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return 400 when ID format is invalid")
        void shouldReturn400WhenIdFormatIsInvalid() throws Exception {
            // Act/Assert
            mockMvc.perform(get("/api/admin/users/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.error.message").value("Invalid value for parameter: id"));
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("updateUserById")
    class UpdateUserByIdTests {
        static Stream<Arguments> invalidUpdateRequest() {
            return Stream.of(
                    Arguments.of(
                            "Name null",
                            new UserUpdateByAdminDTO(null, "user@test.com", List.of(Role.ROLE_USER), true)
                    ),
                    Arguments.of(
                            "Name blank",
                            new UserUpdateByAdminDTO("  ", "user@test.com", null, null)
                    ),
                    Arguments.of(
                            "Email null",
                            new UserUpdateByAdminDTO("User", null, null, null)
                    ),
                    Arguments.of(
                            "Email null",
                            new UserUpdateByAdminDTO("User", "  ", null, null)
                    ),
                    Arguments.of(
                            "Email invalid",
                            new UserUpdateByAdminDTO("User", "user_test.com@", null, null)
                    )
            );
        }

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("New Name", "new@test.com",
                    List.of(Role.ROLE_ADMIN), true);
            UserResponseDTO response = new UserResponseDTO(userId, "New Name", "new@test.com",
                    List.of(Role.ROLE_ADMIN.toString()), true);
            when(userService.updateByAdmin(eq(userId), any(UserUpdateByAdminDTO.class))).thenReturn(response);

            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("New Name"))
                    .andExpect(jsonPath("$.data.enabled").value(true));
        }

        @ParameterizedTest(name = "Invalid request case: {0}")
        @MethodSource("invalidUpdateRequest")
        @DisplayName("Should return 400 when update data is invalid")
        void shouldReturn400WhenInvalidFields(@SuppressWarnings("unused") String caseName,
                UserUpdateByAdminDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should return 404 when user to update does not exist")
        void shouldReturn404WhenUserToUpdateNotExist() throws Exception {
            // Arrange
            Long userId = 99L;
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Name", "user@test.com", List.of(Role.ROLE_USER),
                    true);

            when(userService.updateByAdmin(eq(userId), any(UserUpdateByAdminDTO.class)))
                    .thenThrow(new NotFoundException("User not found"));

            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("resetUserPassword")
    class ResetUserPasswordTests {

        @Test
        @DisplayName("Should reset user password successfully")
        void shouldResetUserPasswordSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            var request = new ResetPasswordByAdminDTO("newPassword!23");

            doNothing().when(userService).resetPasswordByAdmin(userId, "newPassword!23");

            // Act / Assert
            mockMvc.perform(patch("/api/admin/users/{id}/reset-password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data")
                            .value("User password has been reset by administrator"));

            verify(userService).resetPasswordByAdmin(userId, "newPassword!23");
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            var request = new ResetPasswordByAdminDTO("newPassword!23");

            doThrow(new NotFoundException("User not found"))
                    .when(userService).resetPasswordByAdmin(userId, "newPassword!23");

            // Act / Assert
            mockMvc.perform(patch("/api/admin/users/{id}/reset-password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Arrange
            Long userId = 1L;
            var request = new ResetPasswordByAdminDTO("");

            // Act / Assert
            mockMvc.perform(patch("/api/admin/users/{id}/reset-password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("disableUser")
    class DisableUserTests {
        @Test
        @DisplayName("Should disable user successfully")
        void shouldDisableUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            doNothing().when(userService).disableUser(userId);

            // ActAssert
            mockMvc.perform(patch("/api/admin/users/{id}/disable", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("User disabled successfully"));
            verify(userService, times(1)).disableUser(userId);
        }

        @Test
        @DisplayName("Should return 404 when disabling non-existent user")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            doThrow(new NotFoundException("User not found"))
                    .when(userService).disableUser(userId);

            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/{id}/disable", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void shouldReturn400WhenIdIsNotNumber() throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/abc/disable"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("enableUser")
    class EnableUserTests {
        @Test
        @DisplayName("Should enable user successfully")
        void shouldEnableUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            doNothing().when(userService).enableUser(userId);

            // ActAssert
            mockMvc.perform(patch("/api/admin/users/{id}/enable", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("User enabled successfully"));
            verify(userService, times(1)).enableUser(userId);
        }

        @Test
        @DisplayName("Should return 404 when disabling non-existent user")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            doThrow(new NotFoundException("User not found"))
                    .when(userService).enableUser(userId);

            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/{id}/enable", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void shouldReturn400WhenIdIsNotNumber() throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/admin/users/abc/enable"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasksTests {
        @Test
        @DisplayName("Should return tasks filtered by status and priority")
        void shouldReturnTasksFilteredByStatusAndPriority() throws Exception {
            // Arrange
            TaskResponseDTO task = new TaskResponseDTO(
                    1L, "Test Task", "Desc", TaskStatus.IN_PROGRESS, Priority.HIGH, null);
            Page<TaskResponseDTO> page = new PageImpl<>(List.of(task));

            when(taskService.getTasks(eq(false), eq(TaskStatus.IN_PROGRESS), eq(Priority.HIGH), any(), any()))
                    .thenReturn(page);

            // Act/Assert
            mockMvc.perform(get("/api/admin/tasks")
                            .param("deleted", "false")
                            .param("status", "IN_PROGRESS")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("Should return 400 when status enum value is invalid")
        void shouldReturn400WhenInvalidEnumValue() throws Exception {
            // Act/Assert
            mockMvc.perform(get("/api/admin/tasks")
                            .param("status", "NOT_A_STATUS"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.error.message").value(containsString("Allowed values")));
            verifyNoInteractions(taskService);
        }

        @Test
        @DisplayName("Should use default pagination when no params provided")
        void shouldUseDefaultPaginationWhenNoParamsProvided() throws Exception {
            // Arrange
            Page<TaskResponseDTO> emptyPage = new PageImpl<>(List.of());
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(taskService.getTasks(any(), any(), any(), any(), pageableCaptor.capture()))
                    .thenReturn(emptyPage);

            // Act
            mockMvc.perform(get("/api/admin/tasks")).andExpect(status().isOk());

            // Assert
            Pageable pageable = pageableCaptor.getValue();
            assertEquals("dueDate: DESC", pageable.getSort().toString());
        }
    }

    @Nested
    @DisplayName("getSessions")
    class GetSessionsTests {
        @Test
        @DisplayName("Should return sessions even if refreshToken cookie is missing")
        void shouldReturnSessionsEvenWithoutRefreshToken() throws Exception {
            // Arrange
            Long userId = 1L;
            when(sessionService.getSessions(eq(userId), isNull()))
                    .thenReturn(List.of());

            // Act/Assert
            mockMvc.perform(get("/api/admin/users/{userId}/sessions", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 404 when user ID for sessions does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            when(sessionService.getSessions(eq(userId), any()))
                    .thenThrow(new NotFoundException("User not found"));

            // Act/Assert
            mockMvc.perform(get("/api/admin/users/{userId}/sessions", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("User not found"));
        }
    }

    @Nested
    @DisplayName("revokeAllSessions")
    class RevokeAllSessionsTests {
        @Test
        @DisplayName("Should revoke all sessions from a user successfully")
        void shouldRevokeAllSessions() throws Exception {
            // Arrange
            Long userId = 1L;
            doNothing().when(sessionService).revokeAllSessions(userId);

            // Act/Assert
            mockMvc.perform(delete("/api/admin/users/{userId}/sessions", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("All sessions revoked successfully"));
            verify(sessionService, times(1)).revokeAllSessions(userId);
        }

        @Test
        @DisplayName("Should return 404 when user ID does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            doThrow(new NotFoundException("User not found"))
                    .when(sessionService).revokeAllSessions(userId);

            // Act/Assert
            mockMvc.perform(delete("/api/admin/users/{userId}/sessions", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("revokeSession")
    class revokeSessionTests {
        @Test
        @DisplayName("Should revoke specific session from a user successfully")
        void shouldRevokeSpecificSessionSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            Long sessionId = 100L;
            doNothing().when(sessionService).adminRevokeSession(userId, sessionId);

            // Act/Assert
            mockMvc.perform(delete("/api/admin/users/{userId}/sessions/{sessionId}", userId, sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Session revoked successfully"));
            verify(sessionService, times(1)).adminRevokeSession(userId, sessionId);
        }

        @Test
        @DisplayName("Should return 404 when session or user does not exist")
        void shouldReturn404WhenSessionOrUserNotFound() throws Exception {
            // Arrange
            Long userId = 1L;
            Long sessionId = 999L;
            doThrow(new NotFoundException("Session not found"))
                    .when(sessionService).adminRevokeSession(eq(userId), eq(sessionId));

            // Act/Assert
            mockMvc.perform(delete("/api/admin/users/{userId}/sessions/{sessionId}", userId, sessionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }
}
