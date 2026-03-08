package com.christian.taskmanager.service;

import com.christian.taskmanager.entity.User;

public interface CurrentUserService {

    User getCurrentUser();

    Long getCurrentUserId();

    boolean isAdmin();

    void checkOwnershipOrAdmin(Long userId);
}
