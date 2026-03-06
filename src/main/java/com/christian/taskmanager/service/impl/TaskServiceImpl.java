package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.mapper.TaskMapper;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.repository.specification.TaskSpecification;
import com.christian.taskmanager.service.TaskService;
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
        Task task = TaskMapper.toEntity(request);
        Task saved = taskRepository.save(task);
        return TaskMapper.toDTO(saved);
    }

    @Override
    public Page<TaskResponseDTO> getTasks(TaskStatus status, Priority priority, Pageable pageable) {
        Specification<Task> spec = Specification
                .where(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));

        return taskRepository
                .findAll(spec, pageable)
                .map(TaskMapper::toDTO);
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(TaskMapper::toDTO)
                .orElseThrow();
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
