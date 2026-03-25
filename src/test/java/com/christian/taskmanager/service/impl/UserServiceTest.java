package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.PasswordChangeRequestDTO;
import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.EmailAlreadyExistsException;
import com.christian.taskmanager.exception.InvalidCredentialsException;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // Helpers
    private static User createUser(Long id, String name, String email, List<Role> roles) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .roles(roles)
                .build();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {
        @Test
        @DisplayName("Should create a user")
        void shouldCreateUser() {
            // Arrange
            UserCreateDTO request = new UserCreateDTO("test user", "user@test.com", "password",
                    List.of(Role.ROLE_USER), true);

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UserResponseDTO response = userService.create(request);

            // Assert
            assertNotNull(response);
            assertEquals("test user", response.name());
            assertEquals("user@test.com", response.email());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertEquals("test user", savedUser.getName());
            assertEquals("user@test.com", savedUser.getEmail());
            assertEquals("hashedPassword", savedUser.getPassword());
            assertEquals(List.of(Role.ROLE_USER), savedUser.getRoles());
            assertTrue(savedUser.isEnabled());
            verify(passwordEncoder, times(1)).encode("password");
        }

        @Test
        @DisplayName("Should create a user enabled by default when field is null")
        void shouldCreateUserEnabledByDefault() {
            // Arrange
            UserCreateDTO request = new UserCreateDTO("test user", "user@test.com", "password",
                    List.of(Role.ROLE_USER), null);

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UserResponseDTO response = userService.create(request);

            // Assert
            assertNotNull(response);
            assertEquals("test user", response.name());
            assertEquals("user@test.com", response.email());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertEquals("test user", savedUser.getName());
            assertTrue(savedUser.isEnabled());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            UserCreateDTO request = new UserCreateDTO("test user", "user@test.com", "password",
                    List.of(Role.ROLE_USER), true);

            when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

            // Act/Assert
            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already registered");
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsersTest {
        @Test
        @DisplayName("Should return users with no filters")
        void shouldReturnUsersWithNoFilters() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            User user1 = createUser(1L, "test user", "user@test.com", List.of(Role.ROLE_USER));
            User user2 = createUser(2L, "test user 2", "user2@test.com", List.of(Role.ROLE_USER));
            Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable))).thenReturn(userPage);

            // Act
            Page<UserListResponseDTO> response = userService.getUsers(null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getTotalElements());
            assertEquals(2, response.getContent().size());
            assertEquals("user@test.com", response.getContent().get(0).email());
            assertEquals("user2@test.com", response.getContent().get(1).email());

            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return filtered users when name is provided")
        void shouldReturnFilteredUsersByName() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            String filterName = "name";
            User user = createUser(1L, filterName, "name@test.com", List.of(Role.ROLE_USER));
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable))).thenReturn(userPage);

            // Act
            Page<UserListResponseDTO> response = userService.getUsers(filterName, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals(filterName, response.getContent().getFirst().name());
            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return empty page when no users match criteria")
        void shouldReturnEmptyPageWhenNoResults() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable))).thenReturn(emptyPage);

            // Act
            Page<UserListResponseDTO> response = userService.getUsers("NonExistentName", null, null, pageable);

            // Assert
            assertNotNull(response);
            assertTrue(response.getContent().isEmpty());
            assertEquals(0, response.getTotalElements());
            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getUsersById")
    class GetUserById {
        @Test
        @DisplayName("Should return current user when requested id matches current user id")
        void shouldReturnCurrentUserWhenIdMatches() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Christian", "user@test.com", List.of(Role.ROLE_USER));

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(currentUserService.getCurrentUser()).thenReturn(user);

            // Act
            UserResponseDTO result = userService.getUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.id());
            verify(currentUserService).getCurrentUser();
            verify(userRepository, never()).findByIdWithRoles(any());
        }

        @Test
        @DisplayName("Should return user from repository when requested id is different from current user")
        void shouldReturnUserFromRepositoryWhenIdIsDifferent() {
            // Arrange
            Long currentUserId = 1L;
            Long requestedId = 2L;
            User user = createUser(requestedId, "Other User", "other@test.com", List.of(Role.ROLE_USER));

            when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
            when(userRepository.findByIdWithRoles(requestedId)).thenReturn(Optional.of(user));

            // Act
            UserResponseDTO result = userService.getUserById(requestedId);

            // Assert
            assertNotNull(result);
            assertEquals(requestedId, result.id());
            verify(userRepository).findByIdWithRoles(requestedId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Arrange
            Long currentUserId = 1L;
            Long requestedId = 2L;

            when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
            when(userRepository.findByIdWithRoles(requestedId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> userService.getUserById(requestedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");
            verify(userRepository).findByIdWithRoles(requestedId);
        }
    }

    @Nested
    @DisplayName("updateByAdmin")
    class UpdateByAdmin {
        @Test
        @DisplayName("Should update user successfully when admin updates another user")
        void shouldUpdateUserWhenAdminUpdatesAnotherUser() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "test@mail.com", List.of(Role.ROLE_USER));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("New Name", "new@mail.com",
                    List.of(Role.ROLE_USER), true);

            when(userRepository.findByIdWithRoles(targetId)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            UserResponseDTO result = userService.updateByAdmin(targetId, request);

            // Assert
            assertNotNull(result);
            assertEquals("New Name", user.getName());
            assertEquals("new@mail.com", user.getEmail());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should allow admin to update himself when keeping admin role and enabled")
        void shouldAllowAdminToUpdateHimself() {
            // Arrange
            Long id = 1L;
            User user = createUser(id, "Name", "mail@test.com", List.of(Role.ROLE_ADMIN));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("New", "new@mail.com", List.of(Role.ROLE_ADMIN),
                    true);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(id);
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            // Act
            UserResponseDTO response = userService.updateByAdmin(id, request);

            // Assert
            assertNotNull(response);
            assertEquals("New", user.getName());
            assertEquals("new@mail.com", user.getEmail());
            assertEquals("New", response.name());
            assertEquals("new@mail.com", response.email());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should allow admin to update himself when enabled is null")
        void shouldAllowAdminToUpdateHimselfWhenEnabledIsNull() {
            Long id = 1L;
            User user = createUser(id, "Name", "mail@test.com", List.of(Role.ROLE_ADMIN));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("New", "new@mail.com", List.of(Role.ROLE_ADMIN),
                    null);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(id);
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            userService.updateByAdmin(id, request);

            verify(userRepository).save(user);
            assertTrue(user.isEnabled());
        }

        @Test
        @DisplayName("Should throw exception when admin tries to disable himself")
        void shouldThrowExceptionWhenAdminDisablesSelf() {
            // Arrange
            Long id = 1L;
            User user = new User();
            user.setId(id);
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Name", "mail@test.com", List.of(Role.ROLE_ADMIN)
                    , false);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(id);

            // Act/Assert
            assertThatThrownBy(() -> userService.updateByAdmin(id, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot disable yourself");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when admin removes his own admin role")
        void shouldThrowExceptionWhenAdminRemovesOwnAdminRole() {
            // Arrange
            Long id = 1L;
            User user = new User();
            user.setId(id);
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Name", "mail@test.com", List.of(Role.ROLE_USER),
                    true);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(id);

            assertThatThrownBy(() -> userService.updateByAdmin(id, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot remove admin role from yourself");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Arrange
            Long id = 1L;
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Name", "mail@test.com", List.of(Role.ROLE_USER),
                    true);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> userService.updateByAdmin(id, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should update basic fields when roles and enabled are null")
        void shouldUpdateBasicFieldsWhenRolesAndEnabledAreNull() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(2L, "Name", "test@mail.com", List.of(Role.ROLE_USER));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Updated Name", "updated@mail.com", null, null);

            when(userRepository.findByIdWithRoles(targetId)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userRepository.existsByEmail("updated@mail.com")).thenReturn(false);

            // Act
            UserResponseDTO result = userService.updateByAdmin(targetId, request);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Name", user.getName());
            assertEquals("updated@mail.com", user.getEmail());
            assertEquals(List.of(Role.ROLE_USER), user.getRoles());
            assertTrue(user.isEnabled());
            verify(userRepository).save(argThat(u -> u.getId().equals(targetId)));
        }

        @Test
        @DisplayName("Should throw exception when updating email to one that already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "old@mail.com", List.of(Role.ROLE_USER));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Name", "existing@mail.com",
                    List.of(Role.ROLE_USER), true);

            when(userRepository.findByIdWithRoles(targetId)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(userRepository.existsByEmail("existing@mail.com")).thenReturn(true);

            // Act / Assert
            assertThatThrownBy(() -> userService.updateByAdmin(targetId, request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update user when email remains the same")
        void shouldUpdateUserWhenEmailRemainsTheSame() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "same@mail.com", List.of(Role.ROLE_USER));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Updated Name", "same@mail.com",
                    List.of(Role.ROLE_USER), true);

            when(userRepository.findByIdWithRoles(targetId)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(userRepository.save(any())).thenReturn(user);

            // Act
            userService.updateByAdmin(targetId, request);

            // Assert
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should not update roles when roles list is empty")
        void shouldNotUpdateRolesWhenRolesAreEmpty() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("Updated", "new@mail.com", List.of(), true);

            when(userRepository.findByIdWithRoles(targetId)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            // Act
            userService.updateByAdmin(targetId, request);

            // Assert
            assertEquals(List.of(Role.ROLE_USER), user.getRoles());
        }

        @Test
        @DisplayName("Should allow admin to update himself when roles are null")
        void shouldAllowAdminToUpdateHimselfWhenRolesAreNull() {
            // Arrange
            Long id = 1L;
            User user = createUser(id, "Name", "mail@test.com", List.of(Role.ROLE_ADMIN));
            UserUpdateByAdminDTO request = new UserUpdateByAdminDTO("New", "new@mail.com", null, true);

            when(userRepository.findByIdWithRoles(id)).thenReturn(Optional.of(user));
            when(currentUserService.getCurrentUserId()).thenReturn(id);
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            // Act
            userService.updateByAdmin(id, request);

            // Assert
            verify(userRepository).save(argThat(u -> u.getRoles().contains(Role.ROLE_ADMIN)));
            assertEquals(List.of(Role.ROLE_ADMIN), user.getRoles());
        }
    }

    @Nested
    @DisplayName("updateBySelf")
    class UpdateBySelf {
        @Test
        @DisplayName("Should update name and keep same email without checking database")
        void shouldUpdateUserWhenEmailIsSame() {
            // Arrange
            Long userId = 1L;
            String sameEmail = "same@mail.com";
            User user = createUser(userId, "Old Name", sameEmail, List.of(Role.ROLE_USER));
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("New Name", sameEmail);

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            userService.updateBySelf(request);

            // Assert
            assertEquals("New Name", user.getName());
            assertEquals(sameEmail, user.getEmail());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should update when email is different but does not exist in database")
        void shouldUpdateWhenEmailIsDifferentAndNotExists() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Name", "old@mail.com", List.of(Role.ROLE_USER));
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("New Name", "new@mail.com");

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            userService.updateBySelf(request);

            // Assert
            assertEquals("New Name", user.getName());
            assertEquals("new@mail.com", user.getEmail());

            verify(userRepository).existsByEmail("new@mail.com");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw NotFoundException when current user does not exist")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Arrange
            Long userId = 1L;
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("Name", "mail@test.com");

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> userService.updateBySelf(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating to an email that already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Name", "old@mail.com", List.of(Role.ROLE_USER));
            UserUpdateBySelfDTO request = new UserUpdateBySelfDTO("Name", "existing@mail.com");

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("existing@mail.com")).thenReturn(true);

            // Act / Assert
            assertThatThrownBy(() -> userService.updateBySelf(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("disableUser")
    class DisableUser {
        @Test
        @DisplayName("Should disable user successfully")
        void shouldDisableUserSuccessfully() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "mail@test.com", List.of(Role.ROLE_USER));

            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(currentUserService.isAdmin()).thenReturn(true);
            doNothing().when(currentUserService).checkOwnershipOrAdmin(targetId);
            when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

            // Act
            userService.disableUser(targetId);

            // Assert
            assertFalse(user.isEnabled());
            verify(currentUserService).checkOwnershipOrAdmin(targetId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should disable own account when user is not admin")
        void shouldDisableOwnAccountWhenUserIsNotAdmin() {
            // Arrange
            Long id = 1L;
            User user = createUser(id, "Name", "mail@test.com", List.of(Role.ROLE_USER));

            when(currentUserService.getCurrentUserId()).thenReturn(id);
            when(currentUserService.isAdmin()).thenReturn(false);
            doNothing().when(currentUserService).checkOwnershipOrAdmin(id);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            // Act
            userService.disableUser(id);

            // Assert
            assertFalse(user.isEnabled());
            verify(currentUserService).checkOwnershipOrAdmin(id);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw exception when admin tries to disable himself")
        void shouldThrowExceptionWhenAdminDisablesSelf() {
            // Arrange
            Long id = 1L;
            when(currentUserService.getCurrentUserId()).thenReturn(id);
            when(currentUserService.isAdmin()).thenReturn(true);
            doNothing().when(currentUserService).checkOwnershipOrAdmin(id);

            // Act/Assert
            assertThatThrownBy(() -> userService.disableUser(id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot disable yourself");
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(currentUserService.isAdmin()).thenReturn(true);
            doNothing().when(currentUserService).checkOwnershipOrAdmin(targetId);
            when(userRepository.findById(targetId)).thenReturn(Optional.empty());

            // Act / Assert
            assertThatThrownBy(() -> userService.disableUser(targetId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is already disabled")
        void shouldThrowExceptionWhenUserAlreadyDisabled() {
            // Arrange
            Long currentId = 1L;
            Long targetId = 2L;
            User user = createUser(targetId, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            user.setEnabled(false);

            when(currentUserService.getCurrentUserId()).thenReturn(currentId);
            when(currentUserService.isAdmin()).thenReturn(true);
            doNothing().when(currentUserService).checkOwnershipOrAdmin(targetId);
            when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

            // Act / Assert
            assertThatThrownBy(() -> userService.disableUser(targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("User is already disabled");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("enableUser")
    class EnableUser {
        @Test
        @DisplayName("Should enable user successfully")
        void shouldEnableUserSuccessfully() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            user.setEnabled(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            userService.enableUser(userId);

            // Assert
            assertTrue(user.isEnabled());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw exception when user is already enabled")
        void shouldThrowExceptionWhenUserAlreadyEnabled() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            user.setEnabled(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act/Assert
            assertThatThrownBy(() -> userService.enableUser(userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("User is already enabled");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Arrange
            Long userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> userService.enableUser(userId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Arrange
            User user = createUser(1L, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            user.setPassword("encodedOldPassword");

            PasswordChangeRequestDTO request =
                    new PasswordChangeRequestDTO("oldPassword", "newPassword");

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

            // Act
            userService.changePassword(request);

            // Assert
            assertEquals("encodedNewPassword", user.getPassword());
            verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
            verify(passwordEncoder).encode("newPassword");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw exception when old password is incorrect")
        void shouldThrowWhenOldPasswordIsIncorrect() {
            // Arrange
            User user = createUser(1L, "Name", "mail@test.com", List.of(Role.ROLE_USER));
            user.setPassword("encodedOldPassword");

            PasswordChangeRequestDTO request =
                    new PasswordChangeRequestDTO("wrongPassword", "newPassword");

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

            // Act/Assert
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid password");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("resetPasswordByAdmin")
    class ResetPasswordByAdmin {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            // Arrange
            Long userId = 1L;
            User user = createUser(userId, "Name", "mail@test.com", List.of(Role.ROLE_USER));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

            // Act
            userService.resetPasswordByAdmin(userId, "newPassword");

            // Assert
            assertEquals("encodedPassword", user.getPassword());
            verify(passwordEncoder).encode("newPassword");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            Long userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act/Assert
            assertThatThrownBy(() -> userService.resetPasswordByAdmin(userId, "newPassword"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }
}
