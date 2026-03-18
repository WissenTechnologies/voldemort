package com.example.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
