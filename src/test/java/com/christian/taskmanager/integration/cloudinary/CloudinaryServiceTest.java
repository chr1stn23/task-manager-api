package com.christian.taskmanager.integration.cloudinary;

import com.christian.taskmanager.integration.cloudinary.dto.CloudinaryDeleteResponse;
import com.christian.taskmanager.integration.cloudinary.dto.CloudinaryUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    @DisplayName("Should upload file successfully")
    void shouldUploadFileSuccessfully() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);

        byte[] fileBytes = "data".getBytes();
        when(file.getBytes()).thenReturn(fileBytes);

        Map<String, Object> cloudinaryResponse = Map.of(
                "public_id", "testId",
                "secure_url", "http://image.url"
        );

        CloudinaryUploadResponse mappedResponse = new CloudinaryUploadResponse();
        mappedResponse.setPublicId("testId");
        mappedResponse.setSecureUrl("http://image.url");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileBytes), anyMap()))
                .thenReturn(cloudinaryResponse);
        when(objectMapper.convertValue(cloudinaryResponse, CloudinaryUploadResponse.class))
                .thenReturn(mappedResponse);

        // Act
        CloudinaryUploadResponse result =
                cloudinaryService.upload(file, "profiles", "fileName");

        // Assert
        assertNotNull(result);
        assertEquals("testId", result.getPublicId());

        verify(uploader).upload(eq(fileBytes), argThat(map ->
                map.get("folder").equals("profiles") &&
                        map.get("public_id").equals("fileName") &&
                        map.get("resource_type").equals("auto")
        ));
        verify(objectMapper).convertValue(cloudinaryResponse, CloudinaryUploadResponse.class);
    }

    @Test
    @DisplayName("Should throw IOException when upload fails")
    void shouldThrowIOExceptionWhenUploadFails() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);

        when(file.getBytes()).thenThrow(new IOException("error"));

        // Act/Assert
        assertThrows(IOException.class,
                () -> cloudinaryService.upload(file, "profiles", "fileName"));
    }

    @Test
    @DisplayName("Should delete file successfully")
    void shouldDeleteFileSuccessfully() throws Exception {
        // Arrange
        String publicId = "testId";

        Map<String, Object> responseMap = Map.of("result", "ok");

        CloudinaryDeleteResponse mappedResponse = new CloudinaryDeleteResponse();

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap()))
                .thenReturn(responseMap);
        when(objectMapper.convertValue(responseMap, CloudinaryDeleteResponse.class))
                .thenReturn(mappedResponse);

        // Act
        CloudinaryDeleteResponse result = cloudinaryService.delete(publicId);

        // Assert
        assertNotNull(result);
        verify(uploader).destroy(eq(publicId), anyMap());
        verify(objectMapper).convertValue(responseMap, CloudinaryDeleteResponse.class);
    }

    @Test
    @DisplayName("Should throw IOException when delete fails")
    void shouldThrowIOExceptionWhenDeleteFails() throws Exception {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap()))
                .thenThrow(new IOException("error"));

        // Act/Assert
        assertThrows(IOException.class,
                () -> cloudinaryService.delete("id"));
    }
}