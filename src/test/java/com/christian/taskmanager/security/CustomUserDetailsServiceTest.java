package com.christian.taskmanager.security;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Should return user when email exists and user is enabled")
    void shouldReturnUserWhenEmailExistsAndIsEnabled() {
        // Arrange
        String email = "active@test.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);

        when(userRepository.findByEmailWithRoles(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertTrue(result.isEnabled());
        verify(userRepository, times(1)).findByEmailWithRoles(email);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when email does not exist")
    void shouldThrowExceptionWhenEmailDoesNotExist() {
        // Arrange
        String email = "missing@test.com";
        when(userRepository.findByEmailWithRoles(email)).thenReturn(Optional.empty());

        // Act/Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + email);
    }

    @Test
    @DisplayName("Should throw DisabledException when user is found but disabled")
    void shouldThrowExceptionWhenUserIsDisabled() {
        // Arrange
        String email = "disabled@test.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(userRepository.findByEmailWithRoles(email)).thenReturn(Optional.of(user));

        // Act/Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(DisabledException.class)
                .hasMessage("disabled");
    }
}
