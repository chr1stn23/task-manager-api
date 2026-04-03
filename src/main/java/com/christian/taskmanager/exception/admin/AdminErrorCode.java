package com.christian.taskmanager.exception.admin;

import lombok.Getter;

@Getter
public enum AdminErrorCode {
    ADMIN_CANNOT_DISABLE_SELF("Cannot disable yourself"),
    ADMIN_CANNOT_REMOVE_OWN_ADMIN_ROLE("Cannot remove own admin role");

    private final String message;

    AdminErrorCode(String message) {
        this.message = message;
    }

}
