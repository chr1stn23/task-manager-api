package com.christian.taskmanager.repository.specification;

import com.christian.taskmanager.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, cb) ->
                enabled == null ? null : cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<User> isNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> isEmailLike(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }
}