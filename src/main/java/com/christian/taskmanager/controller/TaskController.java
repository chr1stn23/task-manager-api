package com.christian.taskmanager.controller;

import com.christian.taskmanager.common.ApiResponse;
import com.christian.taskmanager.common.ResponseUtil;
import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ApiResponse<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO request) {
        return ResponseUtil.success(taskService.createTask(request));
    }

    @GetMapping
    public ApiResponse<Page<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseUtil.success(taskService.getTasks(status, priority, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseUtil.success(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskRequestDTO request) {
        return ResponseUtil.success(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseUtil.success("Task deleted successfully");
    }
}
