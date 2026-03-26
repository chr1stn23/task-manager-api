package com.christian.taskmanager.mapper;

import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserCreateDTO dto) {
        var builder = User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .nickName(dto.nickName())
                .email(dto.email())
                .roles(dto.roles());

        if (dto.enabled() != null) {
            builder.enabled(dto.enabled());
        }

        return builder.build();
    }

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getNickName(),
                user.getProfileImageUrl(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).toList(),
                user.isEnabled()
        );
    }

    public static UserListResponseDTO toListDTO(User user) {
        return new UserListResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getNickName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.isEnabled()
        );
    }
}
