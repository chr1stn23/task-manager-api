package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.PasswordChangeRequestDTO;
import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.PageResponse;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.*;
import com.christian.taskmanager.exception.admin.AdminBusinessException;
import com.christian.taskmanager.exception.admin.AdminErrorCode;
import com.christian.taskmanager.exception.user.UserBusinessException;
import com.christian.taskmanager.exception.user.UserStateErrorCode;
import com.christian.taskmanager.integration.cloudinary.CloudinaryService;
import com.christian.taskmanager.integration.cloudinary.dto.CloudinaryUploadResponse;
import com.christian.taskmanager.mapper.PageMapper;
import com.christian.taskmanager.mapper.UserMapper;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.repository.specification.UserSpecification;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByNickName(request.nickName())) {
            throw new NickNameAlreadyExistsException("NickName already registered");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        return UserMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserListResponseDTO> getUsers(String searchTerm, String email, Boolean enabled,
            Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.isEnabled(enabled))
                .and(UserSpecification.isNameLike(searchTerm))
                .and(UserSpecification.isEmailLike(email));

        Page<UserListResponseDTO> dtoPage = userRepository
                .findAll(spec, pageable)
                .map(UserMapper::toListDTO);

        return PageMapper.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        User user = Objects.equals(currentUserId, id)
                ? currentUserService.getCurrentUser()
                : userRepository.findByIdWithRoles(id).orElseThrow(() -> new NotFoundException("User not found"));

        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateByAdmin(Long id, UserUpdateByAdminDTO request) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Long currentId = currentUserService.getCurrentUserId();

        if (id.equals(currentId)) {
            if (request.enabled() != null && !request.enabled()) {
                throw new AdminBusinessException(AdminErrorCode.ADMIN_CANNOT_DISABLE_SELF);
            }

            if (request.roles() != null && !request.roles().contains(Role.ROLE_ADMIN)) {
                throw new AdminBusinessException(AdminErrorCode.ADMIN_CANNOT_REMOVE_OWN_ADMIN_ROLE);
            }
        }

        validateEmailUniqueness(request.email(), user.getEmail());
        validateNicknameUniqueness(request.nickName(), user.getNickName());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setNickName(request.nickName());
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
    public UserResponseDTO updateBySelf(UserUpdateBySelfDTO request) {
        Long currentUserId = currentUserService.getCurrentUserId();

        User user = userRepository.findByIdWithRoles(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateEmailUniqueness(request.email(), user.getEmail());
        validateNicknameUniqueness(request.nickName(), user.getNickName());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setNickName(request.nickName());
        user.setEmail(request.email());

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        currentUserService.checkOwnershipOrAdmin(id);

        Long currentId = currentUserService.getCurrentUserId();
        boolean isAdmin = currentUserService.isAdmin();

        if (isAdmin && id.equals(currentId)) {
            throw new AdminBusinessException(AdminErrorCode.ADMIN_CANNOT_DISABLE_SELF);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new UserBusinessException(UserStateErrorCode.USER_ALREADY_DISABLED);
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
            throw new UserBusinessException(UserStateErrorCode.USER_ALREADY_ENABLED);
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequestDTO request) {
        User user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPasswordByAdmin(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        int updatedRows = userRepository.updatePassword(id, encodedPassword);

        if (updatedRows == 0) {
            throw new NotFoundException("User not found");
        }
    }

    @Override
    @Transactional
    public String updateProfilePicture(MultipartFile file) {
        User user = currentUserService.getCurrentUser();

        deleteProfileImageIfExists(user.getProfileImageId());

        String fileName = generateProfileImageFileName(user.getNickName());

        CloudinaryUploadResponse response;
        try {
            response = cloudinaryService.upload(file, "profiles", fileName);
        } catch (IOException e) {
            throw new CloudinaryUploadException("Failed to upload the profile image: " + e.getMessage(), e);
        }

        int updatedRows = userRepository.updateProfileImage(user.getId(), response.getPublicId(),
                response.getSecureUrl());
        if (updatedRows == 0) {
            throw new CloudinaryUploadException("Failed to update the profile image");
        }

        return response.getSecureUrl();
    }

    // ===================================================
    //                   PRIVATE METHODS
    // ===================================================

    private void validateEmailUniqueness(String newEmail, String currentEmail) {
        if (!newEmail.equals(currentEmail) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
    }

    private void validateNicknameUniqueness(String newNickname, String currentNickname) {
        if (!newNickname.equals(currentNickname) && userRepository.existsByNickName(newNickname)) {
            throw new NickNameAlreadyExistsException("NickName already registered");
        }
    }

    private String generateProfileImageFileName(String nickname) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return nickname + "_" + timestamp;
    }

    private void deleteProfileImageIfExists(String profileImageId) {
        if (profileImageId != null && !profileImageId.isEmpty()) {
            try {
                cloudinaryService.delete(profileImageId);
            } catch (IOException e) {
                log.warn("Failed to delete the previous image: {}", e.getMessage());
            }
        }
    }
}
