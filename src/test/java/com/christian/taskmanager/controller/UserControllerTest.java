package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.security.JwtAuthenticationFilter;
import com.christian.taskmanager.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserService currentUserService;


    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {
        @Test
        @DisplayName("Should return current user when exists")
        void shouldReturnCurrentUserWhenExists() throws Exception {
            // Arrange
            Long userId = 1L;
            UserResponseDTO response = new UserResponseDTO(
                    userId, "John Doe", "john@mail.com", List.of(Role.ROLE_USER.toString()), true
            );
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userService.getUserById(1L)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.name").value("John Doe"));
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturnNotFoundWhenUserNotFound() throws Exception {
            // Arrange
            Long userId = 99L;
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

            // Act/Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("updateCurrentUser")
    class UpdateCurrentUserTests {
        static Stream<Arguments> invalidUserRequests() {
            return Stream.of(
                    Arguments.of(
                            "Name is null",
                            new UserUpdateBySelfDTO(null, "test@mail.com", "password")
                    ),
                    Arguments.of(
                            "Name is empty",
                            new UserUpdateBySelfDTO("", "test@mail.com", "password")
                    ),
                    Arguments.of(
                            "Name is blank",
                            new UserUpdateBySelfDTO("   ", "test@mail.com", "password")
                    ),
                    Arguments.of(
                            "Email is null",
                            new UserUpdateBySelfDTO("test name", null, "password")
                    ),
                    Arguments.of(
                            "Email is empty",
                            new UserUpdateBySelfDTO("test name", "", "password")
                    ),
                    Arguments.of(
                            "Email is blank",
                            new UserUpdateBySelfDTO("test name", " ", "password")
                    ),
                    Arguments.of(
                            "Email is invalid",
                            new UserUpdateBySelfDTO("test name", "test@", "password")
                    )
            );
        }

        @Test
        @DisplayName("Should update current user successfully")
        void shouldUpdateCurrentUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("New Name", "new@mail.com", "password");
            UserResponseDTO response = new UserResponseDTO(
                    userId, "New Name", "new@mail.com", List.of(Role.ROLE_USER.toString()), true);
            when(userService.updateBySelf(request)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("New Name"))
                    .andExpect(jsonPath("$.data.email").value("new@mail.com"));
            verify(userService).updateBySelf(request);
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturnNotFoundWhenUserNotFound() throws Exception {
            // Arrange
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("test user", "user@test.com", null);
            when(userService.updateBySelf(request)).thenThrow(new NotFoundException("User not found"));

            // Act/Assert
            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @ParameterizedTest(name = "Invalid update request case: {0}")
        @MethodSource("invalidUserRequests")
        @DisplayName("Should return 400 when request body is invalid")
        void shouldReturnBadRequestWhenRequestInvalid(@SuppressWarnings("unused") String caseName,
                UserUpdateBySelfDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("disableUser")
    class DisableUserTests {
        @Test
        @DisplayName("Should disable user successfully")
        void shouldDisableUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 2L;
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            doNothing().when(userService).disableUser(userId);

            // Act/Assert
            mockMvc.perform(patch("/api/users/me/disable"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("User disabled successfully"));
            verify(userService).disableUser(userId);
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            doThrow(new NotFoundException("Task not found"))
                    .when(userService).disableUser(any());

            // Act/Assert
            mockMvc.perform(patch("/api/users/me/disable"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }
    }
}
