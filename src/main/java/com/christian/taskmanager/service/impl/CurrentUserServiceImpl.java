package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.service.CurrentUserService;
import com.christian.taskmanager.util.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    @Override
    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUser().getId();
    }

    @Override
    public boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }

    @Override
    public void checkOwnershipOrAdmin(Long userId) {
        SecurityUtils.checkTaskOwnershipOrAdmin(userId);
    }
}
