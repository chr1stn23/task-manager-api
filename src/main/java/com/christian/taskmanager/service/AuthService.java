package com.christian.taskmanager.service;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.service.model.TokenPair;

public interface AuthService {

    TokenPair register(RegisterRequestDTO request, String userAgent, String ipAddress);

    TokenPair login(AuthRequestDTO request, String userAgent, String ipAddress);

    TokenPair refresh(String refreshToken, String userAgent, String ipAddress);

    void logout(String refreshToken);
}
