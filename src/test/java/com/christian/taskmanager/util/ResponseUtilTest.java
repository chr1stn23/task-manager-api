package com.christian.taskmanager.util;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseUtilTest {

    @Test
    @DisplayName("Should return a success response with data")
    void success_ShouldReturnValidWrapper() {
        // Arrange
        String testData = "Operación exitosa";

        // Act
        ApiResponseWrapper<String> response = ResponseUtils.success(testData);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("Should return an error response with message and code")
    void error_ShouldReturnValidErrorWrapper() {
        // Arrange
        String message = "Resource not found";
        String code = "NOT_FOUND";

        // Act
        ApiResponseWrapper<Void> response = ResponseUtils.error(message, code);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getMessage()).isEqualTo(message);
        assertThat(response.getError().getCode()).isEqualTo(code);
    }
}
