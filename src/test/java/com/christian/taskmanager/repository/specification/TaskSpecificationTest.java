package com.christian.taskmanager.repository.specification;

import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.Task;
import com.christian.taskmanager.entity.TaskStatus;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSpecificationTest {

    @Mock
    private Root<Task> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(path.get(anyString())).thenReturn(path);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
    }

    @Test
    @DisplayName("Should return null when status is null")
    void shouldReturnNullWhenStatusIsNull() {
        Specification<Task> spec = TaskSpecification.hasStatus(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when status is not null")
    void shouldReturnPredicateWhenStatusNotNull() {
        when(cb.equal(path, TaskStatus.IN_PROGRESS)).thenReturn(predicate);

        Specification<Task> spec = TaskSpecification.hasStatus(TaskStatus.IN_PROGRESS);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should return null when priority is null")
    void shouldReturnNullWhenPriorityIsNull() {
        Specification<Task> spec = TaskSpecification.hasPriority(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when priority is not null")
    void shouldReturnPredicateWhenPriorityNotNull() {
        when(cb.equal(path, Priority.HIGH)).thenReturn(predicate);

        Specification<Task> spec = TaskSpecification.hasPriority(Priority.HIGH);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, Priority.HIGH);
    }

    @Test
    @DisplayName("Should return null when userId is null")
    void shouldReturnNullWhenUserIdIsNull() {
        Specification<Task> spec = TaskSpecification.belongsToUserId(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when userId is not null")
    void shouldReturnPredicateWhenUserIdNotNull() {
        when(cb.equal(path, 1L)).thenReturn(predicate);

        Specification<Task> spec = TaskSpecification.belongsToUserId(1L);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, 1L);
    }

    @Test
    @DisplayName("Should return predicate with false when deleted is null")
    void shouldReturnPredicateWithFalseWhenDeletedIsNull() {
        when(cb.equal(path, false)).thenReturn(predicate);

        Specification<Task> spec = TaskSpecification.isDeleted(null);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, false);
    }

    @Test
    @DisplayName("Should return predicate with true when deleted is true")
    void shouldReturnPredicateWhenDeletedIsTrue() {
        when(cb.equal(path, true)).thenReturn(predicate);

        Specification<Task> spec = TaskSpecification.isDeleted(true);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, true);
    }

    @Test
    @DisplayName("Should return null when search term is blank")
    void shouldReturnNullWhenSearchTermIsBlank() {
        Specification<Task> spec = TaskSpecification.hasSearchTerm("   ");
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return null when search term is null")
    void shouldReturnNullWhenSearchTermIsNull() {
        Specification<Task> spec = TaskSpecification.hasSearchTerm(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return OR predicate for title and description when search term is provided")
    void shouldReturnOrPredicateWhenSearchTermIsProvided() {
        String searchTerm = "search";
        String pattern = "%search%";

        Specification<Task> spec = TaskSpecification.hasSearchTerm(searchTerm);
        spec.toPredicate(root, query, cb);

        verify(root).get("title");
        verify(root).get("description");

        verify(cb, times(2)).like(any(), eq(pattern));
        verify(cb).or(any(), any());
    }
}