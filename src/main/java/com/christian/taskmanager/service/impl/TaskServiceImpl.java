package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.entity.User;
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
                .where(TaskSpecification.belongsToUser(user))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));

        return taskRepository
                .findAll(spec, pageable)
                .map(TaskMapper::toDTO);
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        String email = SecurityUtils.getCurrentUserEmail();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        return TaskMapper.toDTO(task);
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        String email = SecurityUtils.getCurrentUserEmail();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

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
        String email = SecurityUtils.getCurrentUserEmail();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        taskRepository.deleteById(id);
    }
}
