package com.example.demo.services;

import com.example.demo.entities.ResetToken;
import com.example.demo.entities.User;
import com.example.demo.repositories.ResetTokenRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final ResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public PasswordResetService(ResetTokenRepository tokenRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateResetLink(User user) {
        tokenRepository.invalidateAllForUser(user);
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