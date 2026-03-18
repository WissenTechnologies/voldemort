package com.example.demo.Services;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.User;
import com.example.demo.Repository.OtpTokenRepository;

@Service
public class OtpService {

    private final OtpTokenRepository otpRepository;

    public OtpService(OtpTokenRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateAndSaveOtp(User user) {
        otpRepository.deleteByUser(user);
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
        return otpRepository.findByOtpAndUserAndUsedFalse(inputOtp, user)
            .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(t -> { t.setUsed(true); otpRepository.save(t); return true; })
            .orElse(false);
    }
}