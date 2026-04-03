package com.christian.taskmanager.service;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.dto.response.TaskSummaryDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);

    Page<TaskResponseDTO> getTasks(Boolean deleted, String searchTerm, TaskStatus status, Priority priority, Long userId, Pageable pageable);

    TaskResponseDTO getTaskById(Long id);

    TaskResponseDTO updateTask(Long id, TaskRequestDTO request);

    void deleteTask(Long id);

    void restoreTask(Long id);

    TaskSummaryDTO getSummaryByUser(Long userId);
}
