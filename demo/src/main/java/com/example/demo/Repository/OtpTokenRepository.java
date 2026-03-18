package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.User;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByUserAndUsedFalse(User user);
    
    Optional<OtpToken> findByOtpAndUserAndUsedFalse(String otp, User user);
    
    void deleteByUser(User user);
    
    Optional<OtpToken> findTopByUserOrderByCreatedAtDesc(User user);
}
