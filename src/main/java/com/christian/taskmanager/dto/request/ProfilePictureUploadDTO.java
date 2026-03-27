package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.validation.ValidProfileImage;
import org.springframework.web.multipart.MultipartFile;

public record ProfilePictureUploadDTO(
        @ValidProfileImage
        MultipartFile file
) {
}
