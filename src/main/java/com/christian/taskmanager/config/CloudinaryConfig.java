package com.christian.taskmanager.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        return new Cloudinary("cloudinary://" +
                properties.getApiKey() + ":" +
                properties.getApiSecret() + "@" +
                properties.getCloudName());
    }
}
