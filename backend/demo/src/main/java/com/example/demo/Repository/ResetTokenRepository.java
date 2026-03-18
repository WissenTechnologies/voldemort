package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.ResetToken;
import com.example.demo.Entities.User;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByToken(String token);
    
    Optional<ResetToken> findByTokenAndUsedFalse(String token);
    
    void deleteByUser(User user);
}
