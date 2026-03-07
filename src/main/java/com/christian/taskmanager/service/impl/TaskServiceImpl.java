package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.exception.UnauthorizedException;
import com.christian.taskmanager.mapper.TaskMapper;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.repository.specification.TaskSpecification;
import com.christian.taskmanager.service.TaskService;
import com.christian.taskmanager.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        User user = SecurityUtils.getCurrentUser();

        Task task = TaskMapper.toEntity(request);
        task.setUser(user);

        Task saved = taskRepository.save(task);
        return TaskMapper.toDTO(saved);
    }

    @Override
    public Page<TaskResponseDTO> getTasks(TaskStatus status, Priority priority, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();

        Specification<Task> spec = Specification
                .where(TaskSpecification.isDeleted(false))
                .and(TaskSpecification.belongsToUserId(user.getId()))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));

        return taskRepository
                .findAll(spec, pageable)
                .map(TaskMapper::toDTO);
    }

    @Override
    public Page<TaskResponseDTO> getDeletedTasks(TaskStatus status, Priority priority, Long userId, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedException("Only admins can view deleted tasks");
        }

        Specification<Task> spec = Specification
                .where(TaskSpecification.isDeleted(true))
                .and(TaskSpecification.belongsToUserId(userId))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));

        return taskRepository
                .findAll(spec, pageable)
                .map(TaskMapper::toDTO);
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        SecurityUtils.checkTaskOwnershipOrAdmin(task.getUser().getId());

        return TaskMapper.toDTO(task);
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        SecurityUtils.checkTaskOwnershipOrAdmin(task.getUser().getId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        Task updated = taskRepository.save(task);
        return TaskMapper.toDTO(updated);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        SecurityUtils.checkTaskOwnershipOrAdmin(task.getUser().getId());

        task.setDeleted(true);

        taskRepository.save(task);
    }

    @Override
    public void restoreTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedException("Only admins can restore tasks");
        }

        task.setDeleted(false);

        taskRepository.save(task);
    }
}
