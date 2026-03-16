package com.christian.taskmanager.security;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class CurrentUserServiceTest {

    private CurrentUserService currentUserService;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService();
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("Should return current user from SecurityUtils")
    void shouldReturnCurrentUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("test@test.com");

        mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

        // Act
        User result = currentUserService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName("Should return current user ID")
    void shouldReturnCurrentUserId() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(10L);

        mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

        // Act
        Long result = currentUserService.getCurrentUserId();

        // Assert
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("Should return admin status")
    void shouldReturnIsAdmin() {
        // Arrange
        mockedSecurityUtils.when(SecurityUtils::isAdmin).thenReturn(true);

        // Act/Assert
        assertTrue(currentUserService.isAdmin());
    }

    @Test
    @DisplayName("Should call checkOwnershipOrAdmin in SecurityUtils")
    void shouldCallCheckOwnershipOrAdmin() {
        // Arrange
        Long userId = 1L;

        // Act
        currentUserService.checkOwnershipOrAdmin(userId);

        // Assert
        mockedSecurityUtils.verify(() -> SecurityUtils.checkOwnershipOrAdmin(userId));
    }
}
