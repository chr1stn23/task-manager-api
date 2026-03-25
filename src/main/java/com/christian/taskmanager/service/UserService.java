package com.christian.taskmanager.service;

import com.christian.taskmanager.dto.request.PasswordChangeRequestDTO;
import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDTO create(UserCreateDTO request);

    Page<UserListResponseDTO> getUsers(String name, String email, Boolean enabled, Pageable pageable);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateByAdmin(Long id, UserUpdateByAdminDTO request);

    UserResponseDTO updateBySelf(UserUpdateBySelfDTO request);

    void disableUser(Long id);

    void enableUser(Long id);

    void changePassword(PasswordChangeRequestDTO request);

    void resetPasswordByAdmin(Long id, String newPassword);
}
