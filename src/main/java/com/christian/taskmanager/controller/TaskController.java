package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.TaskService;
import com.christian.taskmanager.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ApiResponseWrapper<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO request) {
        return ResponseUtils.success(taskService.createTask(request));
    }

    @Operation(summary = "Get tasks with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ApiResponseWrapper<Page<TaskResponseDTO>> getTasks(
            @Parameter(description = "Filter by deleted status")
            @RequestParam(required = false) Boolean deleted,
            @Parameter(description = "Filter by task status")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by task priority")
            @RequestParam(required = false) Priority priority,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return ResponseUtils.success(taskService.getTasks(deleted, status, priority, currentUserId, pageable));
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ApiResponseWrapper<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseUtils.success(taskService.getTaskById(id));
    }

    @Operation(summary = "Update an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    public ApiResponseWrapper<TaskResponseDTO> updateTask(@PathVariable Long id,
            @RequestBody @Valid TaskRequestDTO request) {
        return ResponseUtils.success(taskService.updateTask(id, request));
    }

    @Operation(summary = "Delete a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}/delete")
    public ApiResponseWrapper<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseUtils.success("Task deleted successfully");
    }

    @Operation(summary = "Restore a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task restored successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}/restore")
    public ApiResponseWrapper<String> restoreTask(@PathVariable Long id) {
        taskService.restoreTask(id);
        return ResponseUtils.success("Task restored successfully");
    }
}
