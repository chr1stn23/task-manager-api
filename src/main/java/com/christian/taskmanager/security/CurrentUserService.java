package com.christian.taskmanager.security;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.util.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUser().getId();
    }

    public boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }

    public void checkOwnershipOrAdmin(Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
    }
}