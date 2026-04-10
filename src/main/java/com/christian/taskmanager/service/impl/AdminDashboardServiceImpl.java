package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.response.AdminDashboardDTO;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AdminDashboardDTO getGlobalStats() {
        Instant now = Instant.now();

        return new AdminDashboardDTO(
                userRepository.countByEnabledTrue(),
                taskRepository.countByDeletedFalse(),
                refreshTokenRepository.countActiveSessions(now),
                taskRepository.countTasksByStatus(),
                taskRepository.countTasksByPriority(),
                taskRepository.findTopUsersByTasksCount(PageRequest.of(0, 5))
        );
    }
}
