package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.EmailAlreadyExistsException;
import com.christian.taskmanager.exception.InvalidCredentialsException;
import com.christian.taskmanager.exception.NickNameAlreadyExistsException;
import com.christian.taskmanager.exception.UserDisabledException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.security.JwtService;
import com.christian.taskmanager.security.RefreshTokenService;
import com.christian.taskmanager.service.model.TokenPair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    // Helpers
    private static User createUser(Long id, String firstName, String email, List<Role> roles) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .nickName(firstName)
                .email(email)
                .roles(roles)
                .build();
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {
        @Test
        @DisplayName("Should register user and return token pair")
        void shouldRegisterUserAndReturnTokenPair() {
            // Arrange
            RegisterRequestDTO request = new RegisterRequestDTO("test user", "Lastname", "nickname", "user@test.com",
                    "password");
            String userAgent = "Windows";
            String ipAddress = "200.48.11.50";

            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(userRepository.existsByNickName("nickname")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(jwtService.generateToken("user@test.com")).thenReturn("accessToken");
            RefreshToken refreshToken = RefreshToken.builder().token("refreshToken").build();
            when(refreshTokenService.createRefreshToken(1L, userAgent, ipAddress)).thenReturn(refreshToken);

            // Act
            TokenPair tokens = authService.register(request, userAgent, ipAddress);

            // Assert
            assertNotNull(tokens);
            assertEquals("accessToken", tokens.accessToken());
            assertEquals("refreshToken", tokens.refreshToken());

            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password");
            verify(jwtService).generateToken("user@test.com");
            verify(refreshTokenService).createRefreshToken(1L, userAgent, ipAddress);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            RegisterRequestDTO request = new RegisterRequestDTO("test user", "LastName", "nickname", "used@test.com",
                    "password");
            String userAgent = "Windows";
            String ipAddress = "200.48.11.50";

            when(userRepository.existsByEmail("used@test.com")).thenReturn(true);

            // Act/Assert
            assertThatThrownBy(() -> authService.register(request, userAgent, ipAddress))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already registered");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when nickname already exists")
        void shouldThrowExceptionWhenNicknameAlreadyExists() {
            // Arrange
            RegisterRequestDTO request = new RegisterRequestDTO("test user", "LastName", "nickname", "used@test.com",
                    "password");
            String userAgent = "Windows";
            String ipAddress = "200.48.11.50";

            when(userRepository.existsByEmail("used@test.com")).thenReturn(false);
            when(userRepository.existsByNickName("nickname")).thenReturn(true);


            // Act/Assert
            assertThatThrownBy(() -> authService.register(request, userAgent, ipAddress))
                    .isInstanceOf(NickNameAlreadyExistsException.class)
                    .hasMessage("Nickname already registered");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {
        @Test
        @DisplayName("Should login user and return token pair")
        void shouldLoginSuccessfullyAndReturnTokens() {
            // Arrange
            String email = "user@test.com";
            AuthRequestDTO request = new AuthRequestDTO(email, "password");
            String userAgent = "Windows";
            String ipAddress = "200.48.11.50";
            User user = createUser(1L, "test user", email, List.of(Role.ROLE_USER));

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
            when(jwtService.generateToken("user@test.com")).thenReturn("accessToken");
            RefreshToken refreshToken = RefreshToken.builder().token("refreshToken").build();
            when(refreshTokenService.createRefreshToken(1L, userAgent, ipAddress)).thenReturn(refreshToken);

            // Act
            TokenPair tokens = authService.login(request, userAgent, ipAddress);

            // Assert
            assertNotNull(tokens);
            assertEquals("accessToken", tokens.accessToken());
            assertEquals("refreshToken", tokens.refreshToken());
            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches("password", user.getPassword());
            verify(jwtService).generateToken("user@test.com");
            verify(refreshTokenService).createRefreshToken(1L, userAgent, ipAddress);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            String email = "unknown@test.com";
            String userAgent = "Windows";
            String ipAddress = "200.48.11.20";
            AuthRequestDTO request = new AuthRequestDTO(email, "password");
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> authService.login(request, userAgent, ipAddress))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid credentials.");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIsIncorrect() {
            // Arrange
            String email = "john@test.com";
            String userAgent = "Windows";
            String ipAddress = "200.48.11.40";
            AuthRequestDTO request = new AuthRequestDTO(email, "wrongPassword");
            User user = createUser(2L, "John Doe", email, List.of(Role.ROLE_USER));

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

            // Act/Assert
            assertThatThrownBy(() -> authService.login(request, userAgent, ipAddress))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid credentials.");
        }

        @Test
        @DisplayName("Should throw exception when user is disabled")
        void shouldThrowExceptionWhenUserIsDisabled() {
            // Arrange
            String email = "mary@test.com";
            String userAgent = "Windows";
            String ipAddress = "200.48.11.10";
            AuthRequestDTO request = new AuthRequestDTO(email, "password");
            User user = createUser(2L, "Mary Hall", email, List.of(Role.ROLE_USER));
            user.setEnabled(false);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

            // Act/Assert
            assertThatThrownBy(() -> authService.login(request, userAgent, ipAddress))
                    .isInstanceOf(UserDisabledException.class)
                    .hasMessage("User is disabled.");
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        @DisplayName("Should refresh token successfully and rotate tokens in order")
        void shouldRefreshTokenSuccessfully() {
            // Arrange
            String oldTokenStr = "old-refresh-token";
            String newAccessToken = "new-access-token";
            String newRefreshTokenStr = "new-refresh-token";
            String userAgent = "Windows";
            String ipAddress = "200.48.11.30";

            User user = createUser(3L, "Joel Smith", "joel@test.com", List.of(Role.ROLE_USER));
            RefreshToken oldRefreshToken = RefreshToken.builder().token(oldTokenStr).user(user).build();
            RefreshToken newRefreshToken = RefreshToken.builder().token(newRefreshTokenStr).user(user).build();

            when(refreshTokenService.findByToken(oldTokenStr)).thenReturn(oldRefreshToken);
            doNothing().when(refreshTokenService).verifyExpiration(oldRefreshToken);
            when(refreshTokenRepository.revokeTokenIfActive(oldTokenStr)).thenReturn(1);
            when(jwtService.generateToken(user.getEmail())).thenReturn(newAccessToken);
            when(refreshTokenService.createRefreshToken(user.getId(), userAgent, ipAddress)).thenReturn(newRefreshToken);

            // Act
            TokenPair tokens = authService.refresh(oldTokenStr, userAgent, ipAddress);

            // Assert
            assertNotNull(tokens);
            assertEquals(newAccessToken, tokens.accessToken());
            assertEquals(newRefreshTokenStr, tokens.refreshToken());

            InOrder inOrder = inOrder(refreshTokenService, refreshTokenRepository, jwtService);
            inOrder.verify(refreshTokenService).findByToken(oldTokenStr);
            inOrder.verify(refreshTokenRepository).revokeTokenIfActive(oldTokenStr);
            inOrder.verify(refreshTokenService).createRefreshToken(user.getId(), userAgent, ipAddress);
            inOrder.verify(jwtService).generateToken(user.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when refresh token is null")
        void shouldThrowExceptionWhenRefreshTokenIsNull() {
            // Arrange
            String userAgent = "Android";
            String ipAddress = "200.48.11.60";

            // Act/Assert
            assertThatThrownBy(() -> authService.refresh(null, userAgent, ipAddress))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Refresh token is required.");
            verify(refreshTokenService, never()).findByToken(any());
        }

        @Test
        @DisplayName("Should throw exception when user is disabled")
        void shouldThrowExceptionWhenUserIsDisabled() {
            // Arrange
            String userAgent = "Windows";
            String ipAddress = "200.48.11.15";
            String oldTokenStr = "old-refresh-token";
            User user = createUser(3L, "Joel Smith", "joel@test.com", List.of(Role.ROLE_USER));
            user.setEnabled(false);
            RefreshToken oldRefreshToken = RefreshToken.builder().token(oldTokenStr).user(user).build();
            when(refreshTokenService.findByToken(any())).thenReturn(oldRefreshToken);
            doNothing().when(refreshTokenService).verifyExpiration(oldRefreshToken);

            // Act/Assert
            assertThatThrownBy(() -> authService.refresh(oldTokenStr, userAgent, ipAddress))
                    .isInstanceOf(UserDisabledException.class)
                    .hasMessage("User is disabled.");
            verify(refreshTokenRepository, never()).revokeTokenIfActive(any());
        }

        @Test
        @DisplayName("Should throw exception when refresh token already used")
        void shouldThrowExceptionWhenRefreshTokenAlreadyUsed() {
            // Arrange
            String userAgent = "Android";
            String ipAddress = "200.48.11.23";
            String oldTokenStr = "old-refresh-token";
            User user = createUser(2L, "John Smith", "john@test.com", List.of(Role.ROLE_USER));
            RefreshToken oldRefreshToken = RefreshToken.builder().token(oldTokenStr).user(user).build();
            when(refreshTokenService.findByToken(any())).thenReturn(oldRefreshToken);
            doNothing().when(refreshTokenService).verifyExpiration(oldRefreshToken);
            when(refreshTokenRepository.revokeTokenIfActive(oldTokenStr)).thenReturn(0);

            // Act/Assert
            assertThatThrownBy(() -> authService.refresh(oldTokenStr, userAgent, ipAddress))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Refresh token already used");
            verify(refreshTokenService, never()).createRefreshToken(user.getId(), userAgent, ipAddress);
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {
        @Test
        @DisplayName("Should logout user successfully")
        void shouldLogoutSuccessfully() {
            // Arrange
            String refreshToken = "old-refresh-token";
            when(refreshTokenRepository.revokeByToken(refreshToken)).thenReturn(1);

            // Act
            authService.logout(refreshToken);

            // Assert
            verify(refreshTokenRepository).revokeByToken(refreshToken);
        }

        @Test
        @DisplayName("Should throw exception when refresh token is null")
        void shouldThrowExceptionWhenRefreshTokenIsNull() {
            // Act/Assert
            assertThatThrownBy(() -> authService.logout(null))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Refresh token is required.");
        }

        @Test
        @DisplayName("Should throw exception when refresh token is invalid")
        void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
            // Arrange
            String refreshToken = "invalid-refresh-token";
            when(refreshTokenRepository.revokeByToken(refreshToken)).thenReturn(0);

            // Act/Assert
            assertThatThrownBy(() -> authService.logout(refreshToken))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid refresh token");
        }
    }
}
