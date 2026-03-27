package com.christian.taskmanager.integration.cloudinary;

import com.christian.taskmanager.integration.cloudinary.dto.CloudinaryDeleteResponse;
import com.christian.taskmanager.integration.cloudinary.dto.CloudinaryUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;

    public CloudinaryUploadResponse upload(MultipartFile file, String folder, String fileName) throws IOException {
        var response = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "public_id", fileName,
                "resource_type", "auto"
        ));

        return objectMapper.convertValue(response, CloudinaryUploadResponse.class);
    }

    public CloudinaryDeleteResponse delete(String publicId) throws IOException {
        var response = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return objectMapper.convertValue(response, CloudinaryDeleteResponse.class);
    }
}
