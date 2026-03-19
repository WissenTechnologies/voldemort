package com.example.order.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);

List<Order> findByUserIdAndStatus(Long userId, String status);

List<Order> findByPortfolioIdAndStatus(Long portfolioId, String status);

List<Order> findByUserId(Long userId);

List<Order> findByPortfolioId(Long portfolioId);
}
