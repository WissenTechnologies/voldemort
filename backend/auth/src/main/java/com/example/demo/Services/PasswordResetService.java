package com.example.demo.Services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.ResetToken;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Repository.ResetTokenRepository;

@Service
public class PasswordResetService {

    private final ResetTokenRepository tokenRepository;
    private final AuthRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public PasswordResetService(ResetTokenRepository tokenRepository,
                                 AuthRepo userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateResetLink(User user) {
        String token = UUID.randomUUID().toString();
        ResetToken rt = ResetToken.builder()
            .user(user)
            .token(token)
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .used(false)
            .build();
        tokenRepository.save(rt);
        return baseUrl + "/reset-password?token=" + token;
    }

    public boolean resetPassword(String token, String newPassword) {
        return tokenRepository.findByTokenAndUsedFalse(token)
            .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(t -> {
                User user = t.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                t.setUsed(true);
                tokenRepository.save(t);
                return true;
            })
            .orElse(false);
    }
}
