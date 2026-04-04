package com.christian.taskmanager.mapper;

import com.christian.taskmanager.dto.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PageMapperTest {

    @Test
    void shouldMapNextAndPreviousPagesCorrectly() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 10);
        Page<String> page = new PageImpl<>(
                List.of("A", "B"),
                pageable,
                30
        );

        // Act
        PageResponse<String> response = PageMapper.from(page);

        // Assert
        assertEquals(1, response.getPage());
        assertEquals(3, response.getTotalPages());

        assertEquals(2, response.getNextPage());
        assertEquals(0, response.getPreviousPage());
    }

    @Test
    void shouldSetNextPageToNullWhenNoNextPage() {
        // Arrange
        Pageable pageable = PageRequest.of(2, 10);
        Page<String> page = new PageImpl<>(
                List.of("A", "B"),
                pageable,
                25
        );

        // Act
        PageResponse<String> response = PageMapper.from(page);

        // Assert
        assertNull(response.getNextPage());
        assertEquals(1, response.getPreviousPage());
    }

    @Test
    void shouldSetPreviousPageToNullWhenFirstPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(
                List.of("A", "B"),
                pageable,
                25
        );

        // Act
        PageResponse<String> response = PageMapper.from(page);

        // Assert
        assertNull(response.getPreviousPage());
        assertEquals(1, response.getNextPage());
    }
}
