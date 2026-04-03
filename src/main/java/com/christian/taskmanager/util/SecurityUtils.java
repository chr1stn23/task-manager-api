package com.christian.taskmanager.util;

import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.UnauthorizedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        return (User) auth.getPrincipal();
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), role));
    }

    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public static void checkOwnershipOrAdmin(Long userId) {
        User currentUser = getCurrentUser();

        boolean isOwner = currentUser.getId() != null && currentUser.getId().equals(userId);

        if (!isOwner && !isAdmin()) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
    }
}
