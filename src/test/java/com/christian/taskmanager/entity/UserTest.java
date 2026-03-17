package com.christian.taskmanager.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    @DisplayName("Should return authorities based on roles")
    void shouldReturnAuthorities() {
        // Arrange
        User user = new User();
        user.setRoles(List.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .containsAll(List.of("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should return email as username")
    void shouldReturnEmailAsUsername() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");

        // Act/Assert
        assertEquals("test@test.com", user.getUsername());
    }
}
