package com.christian.taskmanager.integration.cloudinary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudinaryUploadResponse {
    @JsonProperty("public_id")
    private String publicId;
    private String url;
    @JsonProperty("secure_url")
    private String secureUrl;
    private Long bytes;
    private String format;
    private Integer width;
    private Integer height;
}
