package com.example.demo.repositories;

import com.example.demo.entities.ResetToken;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    Optional<ResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Transactional
    @Query("UPDATE ResetToken r SET r.used = true WHERE r.user = :user AND r.used = false")
    void invalidateAllForUser(User user);
}