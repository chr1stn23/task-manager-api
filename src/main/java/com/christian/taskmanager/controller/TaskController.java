package com.christian.taskmanager.controller;

import com.christian.taskmanager.common.ApiResponseWrapper;
import com.christian.taskmanager.common.ResponseUtil;
import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.service.TaskService;
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

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ApiResponseWrapper<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO request) {
        return ResponseUtil.success(taskService.createTask(request));
    }

    @Operation(summary = "Get tasks with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ApiResponseWrapper<Page<TaskResponseDTO>> getTasks(
            @Parameter(description = "Filter by task status")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by task priority")
            @RequestParam(required = false) Priority priority,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseUtil.success(taskService.getTasks(status, priority, pageable));
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task found"),
            @ApiResponse(responseCode = "400", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ApiResponseWrapper<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseUtil.success(taskService.getTaskById(id));
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
        return ResponseUtil.success(taskService.updateTask(id, request));
    }

    @Operation(summary = "Delete a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    public ApiResponseWrapper<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseUtil.success("Task deleted successfully");
    }
}
