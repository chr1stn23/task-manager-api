package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.NotFoundException;
import com.christian.taskmanager.mapper.UserMapper;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.repository.specification.UserSpecification;
import com.christian.taskmanager.service.CurrentUserService;
import com.christian.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO request) {
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return UserMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsers(String name, String email, Boolean enabled, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.isEnabled(enabled))
                .and(UserSpecification.isNameLike(name))
                .and(UserSpecification.isEmailLike(email));

        return userRepository
                .findAll(spec, pageable)
                .map(UserMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        currentUserService.checkOwnershipOrAdmin(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateByAdmin(Long id, UserUpdateByAdminDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Long currentId = currentUserService.getCurrentUserId();

        if (id.equals(currentId)) {
            if (request.enabled() != null && !request.enabled()) {
                throw new IllegalStateException("Cannot disable yourself");
            }

            if (request.roles() != null && !request.roles().contains(Role.ROLE_ADMIN)) {
                throw new IllegalStateException("Cannot remove admin role from yourself");
            }
        }

        user.setName(request.name());
        user.setEmail(request.email());

        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(request.roles());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateBySelf(Long id, UserUpdateBySelfDTO request) {
        currentUserService.checkOwnershipOrAdmin(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setName(request.name());
        user.setEmail(request.email());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        currentUserService.checkOwnershipOrAdmin(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new IllegalStateException("User is already disabled");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalStateException("User is already enabled");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }
}
