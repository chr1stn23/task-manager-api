package com.christian.taskmanager.util;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.UnauthorizedException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class SecurityUtilTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {
        @Test
        @DisplayName("Should return the current user when authenticated")
        void shouldReturnUser_WhenAuthenticated() {
            // Arrange
            User mockUser = new User();
            mockUser.setId(1L);

            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(mockUser);

            // Act
            User result = SecurityUtils.getCurrentUser();

            // Assert
            assertThat(result).isEqualTo(mockUser);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when not authenticated")
        void shouldThrowException_WhenNotAuthenticated() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(false);

            // Act/Assert
            assertThatThrownBy(SecurityUtils::getCurrentUser)
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not authenticated");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when authentication is null")
        void shouldThrowException_WhenAuthenticationIsNull() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act/Arrange
            assertThatThrownBy(SecurityUtils::getCurrentUser)
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not authenticated");
        }
    }

    @Nested
    @DisplayName("hasRole")
    class HasRoleTests {
        @Test
        @DisplayName("Should return true when user has the specified role")
        void shouldReturnTrue_WhenUserHasRole() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .when(authentication).getAuthorities();

            // Act
            boolean result = SecurityUtils.hasRole("ROLE_ADMIN");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw UnauthorizedException in hasRole when not authenticated")
        void hasRole_ShouldThrowException_WhenNotAuthenticated() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(false);

            // Act/Assert
            assertThatThrownBy(() -> SecurityUtils.hasRole("ROLE_ADMIN"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not authenticated");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException in hasRole when authentication is null")
        void hasRole_ShouldThrowException_WhenAuthenticationIsNull() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act/Assert
            assertThatThrownBy(() -> SecurityUtils.hasRole("ROLE_ADMIN"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not authenticated");
        }
    }

    @Nested
    @DisplayName("isAdmin")
    class IsAdminTests {
        @Test
        @DisplayName("Should return true when user is admin")
        void shouldReturnTrue_WhenUserIsAdmin() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .when(authentication).getAuthorities();

            // Act
            boolean result = SecurityUtils.isAdmin();

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when user is not admin")
        void shouldReturnFalse_WhenUserIsNotAdmin() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .when(authentication).getAuthorities();

            // Act
            boolean result = SecurityUtils.isAdmin();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("checkTaskOwnershipOrAdmin")
    class CheckOwnershipOrAdminTests {
        @Test
        @DisplayName("Should throw exception when not owner and not admin")
        void shouldThrowException_WhenNotOwnerNorAdmin() {
            // Arrange
            User currentUser = new User();
            currentUser.setId(1L);

            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(currentUser);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .when(authentication).getAuthorities();

            // Act/Assert
            assertThatThrownBy(() -> SecurityUtils.checkOwnershipOrAdmin(2L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("You do not have permission to access this resource");
        }

        @Test
        @DisplayName("Should not throw exception when user is the owner")
        void shouldNotThrowException_WhenIsOwner() {
            // Arrange
            User currentUser = new User();
            currentUser.setId(1L);

            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(currentUser);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .when(authentication).getAuthorities();

            // Act
            SecurityUtils.checkOwnershipOrAdmin(1L);
        }

        @Test
        @DisplayName("Should NOT throw exception when is not owner but is ADMIN")
        void shouldNotThrowException_WhenIsAdmin() {
            // Arrange
            User currentUser = new User();
            currentUser.setId(1L);

            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(currentUser);
            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .when(authentication).getAuthorities();

            // Act
            SecurityUtils.checkOwnershipOrAdmin(99L);
        }
    }
}
