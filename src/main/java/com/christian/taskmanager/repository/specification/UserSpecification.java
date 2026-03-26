package com.christian.taskmanager.repository.specification;

import com.christian.taskmanager.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, cb) ->
                enabled == null ? null : cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<User> isNameLike(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) return null;

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            var firstNameLike = cb.like(cb.lower(root.get("firstName")), pattern);
            var lastNameLike = cb.like(cb.lower(root.get("lastName")), pattern);
            var nickNameLike = cb.like(cb.lower(root.get("nickName")), pattern);

            return cb.or(firstNameLike, lastNameLike, nickNameLike);
        };
    }

    public static Specification<User> isEmailLike(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }
}