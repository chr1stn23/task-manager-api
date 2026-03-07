package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.dto.response.AuthResponseDTO;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.EmailAlreadyExistsException;
import com.christian.taskmanager.exception.InvalidCredentialsException;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.security.JwtService;
import com.christian.taskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(token);
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(token);
    }
}
