package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.PasswordChangeRequestDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.exception.CloudinaryUploadException;
import com.christian.taskmanager.exception.InvalidCredentialsException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                    userId, "John", "Doe", "john123", null, "john@mail.com", List.of(Role.ROLE_USER.toString()), true
            );
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userService.getUserById(1L)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.firstName").value("John"))
                    .andExpect(jsonPath("$.data.lastName").value("Doe"));
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
                            "FirstName is null",
                            new UserUpdateBySelfDTO(null, "lastname", "nickname", "test@mail.com")
                    ),
                    Arguments.of(
                            "FirstName is empty",
                            new UserUpdateBySelfDTO("", "lastname", "nickname", "test@mail.com")
                    ),
                    Arguments.of(
                            "FirstName is blank",
                            new UserUpdateBySelfDTO("   ", "lastname", "nickname", "test@mail.com")
                    ),
                    Arguments.of(
                            "Email is null",
                            new UserUpdateBySelfDTO("test name", "lastname", "nickname", null)
                    ),
                    Arguments.of(
                            "Email is empty",
                            new UserUpdateBySelfDTO("test name", "lastname", "nickname", "")
                    ),
                    Arguments.of(
                            "Email is blank",
                            new UserUpdateBySelfDTO("test name", "lastname", "nickname", "  ")
                    ),
                    Arguments.of(
                            "Email is invalid",
                            new UserUpdateBySelfDTO("test name", "lastname", "nickname", "test@"))
            );
        }

        @Test
        @DisplayName("Should update current user successfully")
        void shouldUpdateCurrentUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("New FirstName", "LastName", "nickname", "new@mail" +
                    ".com");
            UserResponseDTO response = new UserResponseDTO(
                    userId, "New FirstName", "LastName", "nickname", null, "new@mail.com",
                    List.of(Role.ROLE_USER.toString()), true);
            when(userService.updateBySelf(request)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("New FirstName"))
                    .andExpect(jsonPath("$.data.email").value("new@mail.com"));
            verify(userService).updateBySelf(request);
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturnNotFoundWhenUserNotFound() throws Exception {
            // Arrange
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("test user", "LastName", "nickname", "user@test.com");
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
    @DisplayName("changePassword")
    class ChangePasswordTests {

        static Stream<Arguments> invalidPasswordRequests() {
            return Stream.of(
                    Arguments.of(
                            "Current password is null",
                            new PasswordChangeRequestDTO(null, "newPassword")
                    ),
                    Arguments.of(
                            "Current password is blank",
                            new PasswordChangeRequestDTO("   ", "newPassword")
                    ),
                    Arguments.of(
                            "New password is null",
                            new PasswordChangeRequestDTO("oldPassword", null)
                    ),
                    Arguments.of(
                            "New password is blank",
                            new PasswordChangeRequestDTO("oldPassword", "   ")
                    ),
                    Arguments.of(
                            "New password is < 8",
                            new PasswordChangeRequestDTO("oldPassword", "short")
                    ),
                    Arguments.of(
                            "New password does not follow the pattern",
                            new PasswordChangeRequestDTO("oldPassword", "newPassword")
                    )
            );
        }

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            // Arrange
            var request = new PasswordChangeRequestDTO("oldPassword", "n3wPassword!");

            doNothing().when(userService).changePassword(request);

            // Act / Assert
            mockMvc.perform(post("/api/users/me/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Password updated successfully"));

            verify(userService).changePassword(request);
        }

        @Test
        @DisplayName("Should return 401 when current password is incorrect")
        void shouldReturnBadRequestWhenPasswordInvalid() throws Exception {
            // Arrange
            var request = new PasswordChangeRequestDTO("wrongPassword", "n3wPassword!");

            doThrow(new InvalidCredentialsException("Invalid password"))
                    .when(userService).changePassword(request);

            // Act/Assert
            mockMvc.perform(post("/api/users/me/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        }

        @ParameterizedTest(name = "Invalid password request case: {0}")
        @MethodSource("invalidPasswordRequests")
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturnBadRequestWhenRequestInvalid(@SuppressWarnings("unused") String caseName,
                PasswordChangeRequestDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/users/me/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("uploadProfilePicture")
    class UploadProfilePictureTests {
        @Test
        @DisplayName("Should upload profile picture successfully")
        void shouldUploadProfilePictureSuccessfully() throws Exception {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "dummy-image-content".getBytes()
            );

            String expectedImageUrl = "https://example.com/profile.jpg";
            when(userService.updateProfilePicture(any())).thenReturn(expectedImageUrl);

            // Act/Assert
            mockMvc.perform(multipart("/api/users/me/upload-picture")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(expectedImageUrl));

            verify(userService).updateProfilePicture(any());
        }

        @Test
        @DisplayName("Should return 400 when file is missing")
        void shouldReturnBadRequestWhenFileMissing() throws Exception {
            // Act/Assert
            mockMvc.perform(multipart("/api/users/me/upload-picture"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when file is invalid")
        void shouldReturn400WhenFileInvalid() throws Exception {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "file.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "invalid".getBytes()
            );

            // Act/Assert
            mockMvc.perform(multipart("/api/users/me/upload-picture")
                            .file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when file exceeds validator size limit")
        void shouldReturn400WhenFileTooLarge() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "big.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    new byte[3 * 1024 * 1024]
            );

            mockMvc.perform(multipart("/api/users/me/upload-picture")
                            .file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 500 when Cloudinary upload fails")
        void shouldReturnInternalServerErrorWhenUploadFails() throws Exception {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "dummy-image-content".getBytes()
            );

            doThrow(new CloudinaryUploadException("Upload failed"))
                    .when(userService).updateProfilePicture(any());

            // Act/Assert
            mockMvc.perform(multipart("/api/users/me/upload-picture")
                            .file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("CLOUDINARY_UPLOAD_ERROR"));
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
