package com.example.demo.services;

import com.example.demo.entities.OtpToken;
import com.example.demo.entities.User;
import com.example.demo.repositories.OtpTokenRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpTokenRepository otpRepository;

    public OtpService(OtpTokenRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateAndSaveOtp(User user) {
        otpRepository.invalidateAllForUser(user);
        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpToken token = OtpToken.builder()
                .user(user)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        otpRepository.save(token);
        return otp;
    }

    public boolean verifyOtp(User user, String inputOtp) {
        return otpRepository.findByUserAndUsedFalse(user)
            .filter(t -> t.getOtp().equals(inputOtp))
            .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(t -> { t.setUsed(true); otpRepository.save(t); return true; })
            .orElse(false);
    }
}