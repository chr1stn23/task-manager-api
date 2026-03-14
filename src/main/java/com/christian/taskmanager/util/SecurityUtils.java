package com.christian.taskmanager.util;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityUtils {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> Objects.equals(auth.getAuthority(), role));
    }

    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public static void checkOwnershipOrAdmin(Long userId) {
        User currentUser = getCurrentUser();

        boolean isOwner = currentUser.getId().equals(userId);
        boolean isAdmin = hasRole("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to access this resource");
        }
    }
}
