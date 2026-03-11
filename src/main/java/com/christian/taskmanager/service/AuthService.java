package com.christian.taskmanager.service;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.service.model.TokenPair;

public interface AuthService {

    TokenPair register(RegisterRequestDTO request);

    TokenPair login(AuthRequestDTO request);

    TokenPair refresh(String refreshToken);

    void logout(String refreshToken);
}
