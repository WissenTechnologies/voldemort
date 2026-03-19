package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.User;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByUserAndUsedFalse(User user);
    
    Optional<OtpToken> findByOtpAndUserAndUsedFalse(String otp, User user);
    
    // Find most recent verified (used) OTP for password reset
    Optional<OtpToken> findTopByUserAndUsedTrueOrderByExpiresAtDesc(User user);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
