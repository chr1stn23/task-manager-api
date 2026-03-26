package com.christian.taskmanager.repository.specification;

import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> belongsToUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Task> isDeleted(Boolean deleted) {
        return (root, query, cb) ->
                cb.equal(root.get("deleted"), Objects.requireNonNullElse(deleted, false));
    }

    public static Specification<Task> hasSearchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) return null;

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            var titleLike = cb.like(cb.lower(root.get("title")), pattern);
            var descriptionLike = cb.like(cb.lower(root.get("description")), pattern);

            return cb.or(titleLike, descriptionLike);
        };
    }
}
