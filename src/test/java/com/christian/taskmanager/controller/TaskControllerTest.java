package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.security.JwtAuthenticationFilter;
import com.christian.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {
        static Stream<Arguments> invalidTaskRequests() {
            return Stream.of(
                    Arguments.of(
                            "Title is null",
                            new TaskRequestDTO(null, "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Title is empty",
                            new TaskRequestDTO("", "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Title is blank",
                            new TaskRequestDTO("   ", "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Status is null",
                            new TaskRequestDTO("Task", "desc", null, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Priority is null",
                            new TaskRequestDTO("Task", "desc", TaskStatus.TODO, null, LocalDate.now())
                    )
            );
        }

        @Test
        @DisplayName("Should create a new task and return a response")
        void shouldCreateATask() throws Exception {
            // Arrange
            TaskRequestDTO request = new TaskRequestDTO(
                    "New task", null, TaskStatus.TODO, Priority.MEDIUM, null);
            TaskResponseDTO response = new TaskResponseDTO(
                    1L, "New Task", null, TaskStatus.TODO, Priority.MEDIUM, null);

            when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(response);

            // Act/Assert
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("New Task"))
                    .andExpect(jsonPath("$.success").value(true));
        }

        @ParameterizedTest(name = "Invalid request case: {0}")
        @MethodSource("invalidTaskRequests")
        @DisplayName("Should return 400 Bad Request when request fields are invalid")
        void shouldReturnBadRequestWhenFieldsAreInvalid(@SuppressWarnings("unused") String caseName,
                TaskRequestDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasksTests {
        @Test
        @DisplayName("Should return tasks page successfully")
        void shouldReturnTasksPage() throws Exception {
            // Assert
            Pageable expectedPageable = PageRequest.of(0, 10, Sort.Direction.DESC, "dueDate");

            Page<TaskResponseDTO> page = new PageImpl<>(List.of(
                    new TaskResponseDTO(1L, "Task 1", null, TaskStatus.TODO, Priority.MEDIUM, null)
            ));

            when(currentUserService.getCurrentUserId()).thenReturn(1L);

            when(taskService.getTasks(null, null, null, 1L, expectedPageable))
                    .thenReturn(page);

            // Act/Assert
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].title").value("Task 1"));

            verify(taskService).getTasks(null, null, null, 1L, expectedPageable);
        }

        @Test
        @DisplayName("Should return tasks filtered by status and priority")
        void shouldReturnTasksFiltered() throws Exception {
            Pageable expectedPageable = PageRequest.of(0, 10, Sort.Direction.DESC, "dueDate");
            Page<TaskResponseDTO> page = new PageImpl<>(List.of());

            when(currentUserService.getCurrentUserId()).thenReturn(1L);
            when(taskService.getTasks(true, TaskStatus.TODO, Priority.HIGH, 1L, expectedPageable))
                    .thenReturn(page);

            // Act/Assert
            mockMvc.perform(get("/api/tasks")
                            .param("deleted", "true")
                            .param("status", "TODO")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk());
            verify(taskService).getTasks(true, TaskStatus.TODO, Priority.HIGH, 1L, expectedPageable);
        }

        @Test
        @DisplayName("Should apply pagination parameters")
        void shouldApplyPagination() throws Exception {
            // Arrange
            when(currentUserService.getCurrentUserId()).thenReturn(1L);
            when(taskService.getTasks(any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());

            // Act/Assert
            mockMvc.perform(get("/api/tasks")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk());
            verify(taskService).getTasks(
                    any(), any(), any(), eq(1L),
                    eq(PageRequest.of(1, 5, Sort.by("dueDate").descending()))
            );
        }

        @Test
        @DisplayName("Should return 400 when status enum is invalid")
        void shouldReturnBadRequestWhenStatusInvalid() throws Exception {

            mockMvc.perform(get("/api/tasks")
                            .param("status", "INVALID"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskByIdTests {
        @Test
        @DisplayName("Should return task when task exists")
        void shouldReturnTaskWhenExists() throws Exception {
            // Arrange
            TaskResponseDTO response = new TaskResponseDTO(
                    1L, "Task 1", null, TaskStatus.TODO, Priority.MEDIUM, null
            );

            when(taskService.getTaskById(1L)).thenReturn(response);

            // Act/Assert
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Task 1"));
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Arrange
            when(taskService.getTaskById(99L))
                    .thenThrow(new NotFoundException("Task not found"));

            // Act/Assert
            mockMvc.perform(get("/api/tasks/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when id is invalid")
        void shouldReturnBadRequestWhenIdInvalid() throws Exception {
            // Act/Assert
            mockMvc.perform(get("/api/tasks/invalid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTests {
        static Stream<Arguments> invalidTaskRequests() {
            return Stream.of(
                    Arguments.of(
                            "Title is null",
                            new TaskRequestDTO(null, "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Title is empty",
                            new TaskRequestDTO("", "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Title is blank",
                            new TaskRequestDTO("   ", "desc", TaskStatus.TODO, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Status is null",
                            new TaskRequestDTO("Task", "desc", null, Priority.MEDIUM, LocalDate.now())
                    ),
                    Arguments.of(
                            "Priority is null",
                            new TaskRequestDTO("Task", "desc", TaskStatus.TODO, null, LocalDate.now())
                    )
            );
        }

        @Test
        @DisplayName("Should update task successfully")
        void shouldUpdateTaskSuccessfully() throws Exception {
            // Arrange
            TaskRequestDTO request = new TaskRequestDTO(
                    "Updated task", "desc", TaskStatus.TODO, Priority.HIGH, null
            );

            TaskResponseDTO response = new TaskResponseDTO(
                    1L, "Updated task", "desc", TaskStatus.TODO, Priority.HIGH, null
            );

            when(taskService.updateTask(eq(1L), any(TaskRequestDTO.class)))
                    .thenReturn(response);

            // Act/Assert
            mockMvc.perform(put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Updated task"));
            verify(taskService).updateTask(eq(1L), any(TaskRequestDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Arrange
            TaskRequestDTO request = new TaskRequestDTO(
                    "Updated task", null, TaskStatus.TODO, Priority.MEDIUM, null
            );

            when(taskService.updateTask(eq(99L), any()))
                    .thenThrow(new NotFoundException("Task not found"));

            // Act/Assert
            mockMvc.perform(put("/api/tasks/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @ParameterizedTest(name = "Invalid update request case: {0}")
        @MethodSource("invalidTaskRequests")
        @DisplayName("Should return 400 when request body is invalid")
        void shouldReturnBadRequestWhenRequestInvalid(@SuppressWarnings("unused") String caseName,
                TaskRequestDTO request) throws Exception {
            // Act/Assert
            mockMvc.perform(put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when id is invalid")
        void shouldReturnBadRequestWhenIdInvalid() throws Exception {
            // Arrange
            TaskRequestDTO request = new TaskRequestDTO(
                    "Task", null, TaskStatus.TODO, Priority.MEDIUM, null
            );

            // Act/Assert
            mockMvc.perform(put("/api/tasks/invalid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTaskTests {
        @Test
        @DisplayName("Should delete task successfully")
        void shouldDeleteTaskSuccessfully() throws Exception {
            // Arrange
            doNothing().when(taskService).deleteTask(1L);

            // Act/Assert
            mockMvc.perform(patch("/api/tasks/1/delete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Task deleted successfully"));
            verify(taskService).deleteTask(1L);
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Arrange
            doThrow(new NotFoundException("Task not found"))
                    .when(taskService).deleteTask(99L);

            // Act/Assert
            mockMvc.perform(patch("/api/tasks/99/delete"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when id is invalid")
        void shouldReturnBadRequestWhenIdInvalid() throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/tasks/invalid/delete"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("restoreTask")
    class RestoreTaskTests {
        @Test
        @DisplayName("Should restore task successfully")
        void shouldRestoreTaskSuccessfully() throws Exception {
            // Arrange
            doNothing().when(taskService).restoreTask(1L);

            // Act/Assert
            mockMvc.perform(patch("/api/tasks/1/restore"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Task restored successfully"));
            verify(taskService).restoreTask(1L);
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Arrange
            doThrow(new NotFoundException("Task not found"))
                    .when(taskService).restoreTask(99L);

            // Act/Assert
            mockMvc.perform(patch("/api/tasks/99/restore"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when id is invalid")
        void shouldReturnBadRequestWhenIdInvalid() throws Exception {
            // Act/Assert
            mockMvc.perform(patch("/api/tasks/invalid/restore"))
                    .andExpect(status().isBadRequest());
        }
    }
}
