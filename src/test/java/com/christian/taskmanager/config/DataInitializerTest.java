package com.christian.taskmanager.config;

import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "Admin123!");
    }

    @Test
    void shouldCreateAdminIfNotExists() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Admin123!"))
                .thenReturn("encodedPassword");

        // Act
        dataInitializer.run(null);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("admin@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRoles()).contains(Role.ROLE_ADMIN);
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    void shouldNotCreateAdminIfAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(new User()));

        // Act
        dataInitializer.run(null);

        // Assert
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
