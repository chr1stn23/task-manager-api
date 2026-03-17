package com.christian.taskmanager.repository.specification;

import com.christian.taskmanager.entity.User;
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
class UserSpecificationTest {

    @Mock
    private Root<User> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<String> expression;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(path.get(anyString())).thenReturn(path);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.lower(any())).thenReturn(expression);
    }

    // isEnabled
    @Test
    @DisplayName("Should return null when enabled is null")
    void shouldReturnNullWhenEnabledIsNull() {
        Specification<User> spec = UserSpecification.isEnabled(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when enabled is true")
    void shouldReturnPredicateWhenEnabledIsTrue() {
        when(cb.equal(path, true)).thenReturn(predicate);

        Specification<User> spec = UserSpecification.isEnabled(true);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, true);
    }

    @Test
    @DisplayName("Should return predicate when enabled is false")
    void shouldReturnPredicateWhenEnabledIsFalse() {
        when(cb.equal(path, false)).thenReturn(predicate);

        Specification<User> spec = UserSpecification.isEnabled(false);
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).equal(path, false);
    }

    // isNameLike
    @Test
    @DisplayName("Should return null when name is null")
    void shouldReturnNullWhenNameIsNull() {
        Specification<User> spec = UserSpecification.isNameLike(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return null when name is blank")
    void shouldReturnNullWhenNameIsBlank() {
        Specification<User> spec = UserSpecification.isNameLike("   ");
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when name is not blank")
    void shouldReturnPredicateWhenNameNotBlank() {
        Specification<User> spec = UserSpecification.isNameLike("mariano");
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).like(expression, "%mariano%");
    }

    // isEmailLike
    @Test
    @DisplayName("Should return null when email is null")
    void shouldReturnNullWhenEmailIsNull() {
        Specification<User> spec = UserSpecification.isEmailLike(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return null when email is blank")
    void shouldReturnNullWhenEmailIsBlank() {
        Specification<User> spec = UserSpecification.isEmailLike("   ");
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @DisplayName("Should return predicate when email is not blank")
    void shouldReturnPredicateWhenEmailNotBlank() {
        Specification<User> spec = UserSpecification.isEmailLike("julio@test.com");
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
        verify(cb).like(expression, "%julio@test.com%");
    }
}