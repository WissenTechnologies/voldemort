package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.OtpToken;
import com.example.demo.entities.User;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByUserAndUsedFalse(User user);

    // Invalidate all previous OTPs for a user before issuing a new one
    @Modifying
    @Transactional
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.user = :user AND o.used = false")
    void invalidateAllForUser(User user);
}