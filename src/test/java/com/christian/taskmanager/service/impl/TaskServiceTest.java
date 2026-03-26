package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.*;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.repository.TaskRepository;
import com.christian.taskmanager.security.CurrentUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TaskServiceImpl taskService;

    // Helpers
    private static User createUser(Long id, String firstName, String email, List<Role> roles) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .nickName(firstName)
                .email(email)
                .roles(roles)
                .build();
    }

    private static Task createTask(Long id, String title, TaskStatus status, Priority priority, boolean deleted) {
        return Task.builder()
                .id(id)
                .title(title)
                .status(status)
                .priority(priority)
                .deleted(deleted)
                .build();
    }

    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {
        @Test
        @DisplayName("Should create a task and assign it to the current user")
        void shouldCreateTaskAndAssignCurrentUser() {
            // Arrange
            User user = createUser(1L, "test user", "user@test.com", List.of(Role.ROLE_USER));
            TaskRequestDTO request = new TaskRequestDTO("test task", null, TaskStatus.TODO, Priority.MEDIUM, null);

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TaskResponseDTO response = taskService.createTask(request);

            // Assert
            assertNotNull(response);
            assertEquals("test task", response.title());

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(captor.capture());

            Task savedTask = captor.getValue();
            assertEquals(user.getId(), savedTask.getUser().getId());
            assertEquals("test task", savedTask.getTitle());
            assertEquals(TaskStatus.TODO, savedTask.getStatus());
            assertEquals(Priority.MEDIUM, savedTask.getPriority());
        }
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasksTests {

        @Test
        @DisplayName("Should return active tasks belonging to the current user")
        void shouldReturnActiveTasksForCurrentUser() {
            // Arrange
            Long userId = 2L;
            TaskStatus status = TaskStatus.IN_PROGRESS;
            Priority priority = Priority.LOW;
            Pageable pageable = PageRequest.of(0, 10);
            User user = createUser(userId, "test user", "user@test.com", List.of(Role.ROLE_USER));
            Task task = createTask(1L, "task 1", status, priority, false);
            task.setUser(user);
            Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);

            when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);

            // Act
            Page<TaskResponseDTO> response = taskService.getTasks(false, status, priority, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            TaskResponseDTO dto = response.getContent().getFirst();
            assertEquals(task.getTitle(), dto.title());
            assertEquals(task.getId(), dto.id());
            assertEquals(task.getStatus(), dto.status());
            assertEquals(task.getPriority(), dto.priority());
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return deleted tasks belonging to the current user (trash)")
        void shouldReturnDeletedTasksForCurrentUser() {
            // Arrange
            Long userId = 2L;
            Pageable pageable = PageRequest.of(0, 10);
            User user = createUser(userId, "test user", "user@test.com", List.of(Role.ROLE_USER));
            Task deletedTask = createTask(1L, "deleted task", TaskStatus.IN_PROGRESS, Priority.LOW, true);
            deletedTask.setUser(user);
            Page<Task> taskPage = new PageImpl<>(List.of(deletedTask), pageable, 1);

            when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);

            // Act
            Page<TaskResponseDTO> response = taskService.getTasks(true, null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            TaskResponseDTO dto = response.getContent().getFirst();
            assertEquals(deletedTask.getTitle(), dto.title());
            assertEquals(deletedTask.getId(), dto.id());
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return empty page when user has no tasks")
        void shouldReturnEmptyPageWhenUserHasNoTasks() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> taskPage = new PageImpl<>(List.of(), pageable, 0);

            when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);

            // Act
            Page<TaskResponseDTO> response = taskService.getTasks(null, null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalElements());
            assertTrue(response.getContent().isEmpty());
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should allow admin to see tasks from any user")
        void shouldAllowAdminToSeeTasksFromAnyUser() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            User user1 = createUser(1L, "user1", "user1@test.com", List.of(Role.ROLE_USER));
            Task task = createTask(1L, "task 1", TaskStatus.TODO, Priority.HIGH, false);
            task.setUser(user1);
            Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);

            when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);

            // Act
            Page<TaskResponseDTO> response = taskService.getTasks(false, null, null, 1L, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals("task 1", response.getContent().getFirst().title());
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should allow admin to see deleted tasks from any user")
        void shouldAllowAdminToSeeDeletedTasksFromAnyUser() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            User user = createUser(1L, "user", "user@test.com", List.of(Role.ROLE_USER));
            Task deletedTask = createTask(1L, "deleted task", TaskStatus.TODO, Priority.HIGH, true);
            deletedTask.setUser(user);
            Page<Task> taskPage = new PageImpl<>(List.of(deletedTask), pageable, 1);

            when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);

            // Act
            Page<TaskResponseDTO> response = taskService.getTasks(true, null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals("deleted task", response.getContent().getFirst().title());
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should return task when task exists and user has access")
        void shouldReturnTaskWhenTaskExistsAndUserHasAccess() {
            // Arrange
            Long taskId = 1L;
            Long userId = 2L;
            Task task = createTask(taskId, "test task", TaskStatus.TODO, Priority.MEDIUM, false);
            task.setUser(createUser(userId, "test user", "user@test.com", List.of(Role.ROLE_USER)));
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));
            doNothing().when(currentUserService).checkOwnershipOrAdmin(userId);

            // Act
            TaskResponseDTO response = taskService.getTaskById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(task.getTitle(), response.title());
            assertEquals(task.getId(), response.id());
            assertEquals(task.getStatus(), response.status());
            assertEquals(task.getPriority(), response.priority());
            verify(taskRepository).findByIdAndDeletedFalse(taskId);
            verify(currentUserService).checkOwnershipOrAdmin(userId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when task does not exist")
        void shouldThrowNotFoundWhenTaskDoesNotExist() {
            // Arrange
            Long taskId = 1L;
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> taskService.getTaskById(taskId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Task not found");
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should update task when task exists and user has permission")
        void shouldUpdateTaskWhenTaskExistsAndUserHasPermission() {
            // Arrange
            Long taskId = 1L;
            Long userId = 2L;
            Task task = createTask(taskId, "test task", TaskStatus.TODO, Priority.MEDIUM, false);
            task.setUser(createUser(userId, "test user", "user@test.com", List.of(Role.ROLE_USER)));
            TaskRequestDTO request = new TaskRequestDTO("updated task", null, TaskStatus.IN_PROGRESS, Priority.HIGH,
                    null);
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));
            doNothing().when(currentUserService).checkOwnershipOrAdmin(userId);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TaskResponseDTO response = taskService.updateTask(taskId, request);

            // Assert
            assertNotNull(response);
            assertEquals("updated task", response.title());
            assertEquals(TaskStatus.IN_PROGRESS, response.status());
            assertEquals(Priority.HIGH, response.priority());

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(captor.capture());
            Task savedTask = captor.getValue();

            assertEquals("updated task", savedTask.getTitle());
            assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());
            assertEquals(Priority.HIGH, savedTask.getPriority());

            verify(currentUserService).checkOwnershipOrAdmin(userId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when task to update does not exist")
        void shouldThrowNotFoundWhenUpdatingNonExistingTask() {
            // Arrange
            Long taskId = 1L;
            TaskRequestDTO request = new TaskRequestDTO("updated task", null, TaskStatus.IN_PROGRESS, Priority.HIGH,
                    null);
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> taskService.updateTask(taskId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Task not found");
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should mark task as deleted when task exists and user has permission")
        void shouldSoftDeleteTaskWhenUserHasPermission() {
            // Arrange
            Long taskId = 2L;
            Long userId = 1L;
            Task task = createTask(taskId, "test task", TaskStatus.TODO, Priority.MEDIUM, false);
            task.setUser(createUser(userId, "test user", "user@test.com", List.of(Role.ROLE_USER)));
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));
            doNothing().when(currentUserService).checkOwnershipOrAdmin(userId);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            taskService.deleteTask(taskId);

            // Assert
            assertTrue(task.isDeleted());
            verify(taskRepository).findByIdAndDeletedFalse(taskId);
            verify(currentUserService).checkOwnershipOrAdmin(userId);
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non existing task")
        void shouldThrowNotFoundWhenDeletingNonExistingTask() {
            // Arrange
            Long taskId = 2L;
            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> taskService.deleteTask(taskId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Task not found");
        }
    }

    @Nested
    @DisplayName("restoreTask")
    class RestoreTaskTests {

        @Test
        @DisplayName("Should restore deleted task when current user is admin")
        void shouldRestoreTaskWhenAdmin() {
            // Arrange
            Long taskId = 2L;
            Long ownerId = 5L;

            User user = createUser(ownerId, "user", "user@test.com", List.of(Role.ROLE_USER));
            Task delTask = createTask(taskId, "task 2", TaskStatus.TODO, Priority.HIGH, true);
            delTask.setUser(user);

            when(taskRepository.findByIdAndDeletedTrue(taskId)).thenReturn(Optional.of(delTask));
            doNothing().when(currentUserService).checkOwnershipOrAdmin(ownerId);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            taskService.restoreTask(taskId);

            // Assert
            assertFalse(delTask.isDeleted());
            verify(taskRepository).findByIdAndDeletedTrue(taskId);
            verify(currentUserService).checkOwnershipOrAdmin(ownerId);
            verify(taskRepository).save(delTask);
        }

        @Test
        @DisplayName("Should restore deleted task when current user is the owner")
        void shouldRestoreTaskWhenOwner() {
            // Arrange
            Long taskId = 1L;
            Long ownerId = 1L;

            User user = createUser(ownerId, "user", "user@test.com", List.of(Role.ROLE_USER));
            Task delTask = createTask(taskId, "task", TaskStatus.TODO, Priority.LOW, true);
            delTask.setUser(user);

            when(taskRepository.findByIdAndDeletedTrue(taskId)).thenReturn(Optional.of(delTask));
            doNothing().when(currentUserService).checkOwnershipOrAdmin(ownerId);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            taskService.restoreTask(taskId);

            // Assert
            assertFalse(delTask.isDeleted());
            verify(taskRepository).findByIdAndDeletedTrue(taskId);
            verify(currentUserService).checkOwnershipOrAdmin(ownerId);
            verify(taskRepository).save(delTask);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleted task does not exist")
        void shouldThrowNotFoundWhenRestoringNonExistingTask() {
            // Arrange
            Long taskId = 2L;

            when(taskRepository.findByIdAndDeletedTrue(taskId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> taskService.restoreTask(taskId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Task not found");

            verify(taskRepository).findByIdAndDeletedTrue(taskId);
        }
    }
}
