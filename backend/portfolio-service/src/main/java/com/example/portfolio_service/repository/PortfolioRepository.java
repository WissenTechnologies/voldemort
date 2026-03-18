package com.example.portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portfolio_service.entites.Portfolio;
import java.util.List;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);
}
