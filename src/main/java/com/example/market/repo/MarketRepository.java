package com.example.market.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.market.entities.MarketShare;

public interface MarketRepository extends JpaRepository<MarketShare, Long> {
}
