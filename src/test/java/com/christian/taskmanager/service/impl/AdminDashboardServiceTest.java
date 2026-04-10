package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.response.AdminDashboardDTO;
import com.christian.taskmanager.dto.response.PriorityCountDTO;
import com.christian.taskmanager.dto.response.StatusCountDTO;
import com.christian.taskmanager.dto.response.UserTaskCountDTO;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AdminDashboardServiceImpl service;

    @Test
    @DisplayName("Should return global stats successfully")
    void shouldReturnGlobalStats() {
        // Arrange
        long activeUsers = 10L;
        long totalTasks = 50L;
        long liveSessions = 5L;

        var statusCounts = List.of(new StatusCountDTO(null, 20L));
        var priorityCounts = List.of(new PriorityCountDTO(null, 15L));
        var topUsers = List.of(new UserTaskCountDTO("miguel", 30L));

        when(userRepository.countByEnabledTrue()).thenReturn(activeUsers);
        when(taskRepository.countByDeletedFalse()).thenReturn(totalTasks);
        when(refreshTokenRepository.countActiveSessions(any(Instant.class)))
                .thenReturn(liveSessions);
        when(taskRepository.countTasksByStatus()).thenReturn(statusCounts);
        when(taskRepository.countTasksByPriority()).thenReturn(priorityCounts);
        when(taskRepository.findTopUsersByTasksCount(PageRequest.of(0, 5)))
                .thenReturn(topUsers);

        // Act
        AdminDashboardDTO result = service.getGlobalStats();

        // Assert
        assertNotNull(result);

        assertEquals(activeUsers, result.activeUsers());
        assertEquals(totalTasks, result.totalTasks());
        assertEquals(liveSessions, result.liveSessions());
        assertEquals(statusCounts, result.tasksByStatus());
        assertEquals(priorityCounts, result.tasksByPriority());
        assertEquals(topUsers, result.topUsers());

        verify(userRepository).countByEnabledTrue();
        verify(taskRepository).countByDeletedFalse();
        verify(refreshTokenRepository).countActiveSessions(any(Instant.class));
        verify(taskRepository).countTasksByStatus();
        verify(taskRepository).countTasksByPriority();
        verify(taskRepository).findTopUsersByTasksCount(PageRequest.of(0, 5));
    }
}
