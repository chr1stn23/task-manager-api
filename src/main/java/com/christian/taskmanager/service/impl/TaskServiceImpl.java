package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.dto.response.TaskSummaryDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.mapper.TaskMapper;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.repository.specification.TaskSpecification;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        User user = currentUserService.getCurrentUser();

        Task task = TaskMapper.toEntity(request);
        task.setUser(user);

        Task saved = taskRepository.save(task);
        return TaskMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasks(Boolean deleted, String searchTerm, TaskStatus status, Priority priority,
            Long userId,
            Pageable pageable) {
        Specification<Task> spec = Specification
                .where(TaskSpecification.isDeleted(deleted))
                .and(TaskSpecification.hasSearchTerm(searchTerm))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.belongsToUserId(userId));

        return taskRepository
                .findAll(spec, pageable)
                .map(TaskMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        currentUserService.checkOwnershipOrAdmin(task.getUser().getId());

        return TaskMapper.toDTO(task);
    }

    @Transactional
    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        currentUserService.checkOwnershipOrAdmin(task.getUser().getId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        Task updated = taskRepository.save(task);
        return TaskMapper.toDTO(updated);
    }

    @Transactional
    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        currentUserService.checkOwnershipOrAdmin(task.getUser().getId());

        task.setDeleted(true);

        taskRepository.save(task);
    }

    @Transactional
    @Override
    public void restoreTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        currentUserService.checkOwnershipOrAdmin(task.getUser().getId());

        task.setDeleted(false);

        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskSummaryDTO getSummaryByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        TaskSummaryDTO summary = taskRepository.getSummaryByUser(userId);
        return summary != null ? summary : new TaskSummaryDTO(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
