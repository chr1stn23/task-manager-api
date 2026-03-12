package com.christian.taskmanager.service.impl;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.entity.RefreshToken;
import com.christian.taskmanager.entity.Role;
import com.christian.taskmanager.entity.User;
import com.christian.taskmanager.exception.EmailAlreadyExistsException;
import com.christian.taskmanager.exception.InvalidCredentialsException;
import com.christian.taskmanager.exception.UserDisabledException;
import com.christian.taskmanager.repository.RefreshTokenRepository;
import com.christian.taskmanager.repository.UserRepository;
import com.christian.taskmanager.security.JwtService;
import com.christian.taskmanager.security.RefreshTokenService;
import com.christian.taskmanager.service.AuthService;
import com.christian.taskmanager.service.model.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public TokenPair register(RegisterRequestDTO request, String userAgent, String ipAddress) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of(Role.ROLE_USER))
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId(), userAgent, ipAddress);

        return new TokenPair(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public TokenPair login(AuthRequestDTO request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        if (!user.isEnabled()) {
            throw new UserDisabledException("User is disabled.");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgent, ipAddress);

        return new TokenPair(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public TokenPair refresh(String refreshToken, String userAgent, String ipAddress) {
        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token is required.");
        }

        RefreshToken token = refreshTokenService.findByToken(refreshToken);
        refreshTokenService.verifyExpiration(token);

        User user = token.getUser();

        if (!user.isEnabled()) {
            throw new UserDisabledException("User is disabled.");
        }

        int updated = refreshTokenRepository.revokeTokenIfActive(refreshToken);

        if (updated == 0) {
            throw new InvalidCredentialsException("Refresh token already used");
        }

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgent, ipAddress);
        String newAccessToken = jwtService.generateToken(user.getEmail());
        return new TokenPair(newAccessToken, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token is required.");
        }

        int updated = refreshTokenRepository.revokeByToken(refreshToken);

        if (updated == 0) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }
}
