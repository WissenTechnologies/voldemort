package com.example.order.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.order.entities.Order;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        order1 = new Order(1L, 1L, 1L, "Apple Inc.", "BUY", 100, 150.0, "SUCCESS");
        order1.setCreatedAt(now);
        
        order2 = new Order(2L, 2L, 2L, "Microsoft Corp.", "SELL", 50, 200.0, "SUCCESS");
        order2.setCreatedAt(now.plusMinutes(5));
    }

    @Test
    void saveOrder_shouldPersistOrder() {
        Order savedOrder = orderRepository.save(order1);

        Order retrievedOrder = entityManager.find(Order.class, savedOrder.getId());
        
        assertNotNull(retrievedOrder);
        assertEquals(savedOrder.getId(), retrievedOrder.getId());
        assertEquals(1L, retrievedOrder.getUserId());
        assertEquals(1L, retrievedOrder.getPortfolioId());
        assertEquals(1L, retrievedOrder.getCompanyId());
        assertEquals("Apple Inc.", retrievedOrder.getCompanyName());
        assertEquals("BUY", retrievedOrder.getType());
        assertEquals(100, retrievedOrder.getQuantity());
        assertEquals(150.0, retrievedOrder.getPrice());
        assertEquals("SUCCESS", retrievedOrder.getStatus());
        assertNotNull(retrievedOrder.getCreatedAt());
    }

    @Test
    void findById_whenOrderExists_shouldReturnOrder() {
        entityManager.persistAndFlush(order1);
        
        Optional<Order> foundOrder = orderRepository.findById(order1.getId());
        
        assertTrue(foundOrder.isPresent());
        assertEquals(order1.getId(), foundOrder.get().getId());
        assertEquals("Apple Inc.", foundOrder.get().getCompanyName());
        assertEquals("BUY", foundOrder.get().getType());
        assertEquals(100, foundOrder.get().getQuantity());
        assertEquals(150.0, foundOrder.get().getPrice());
        assertEquals("SUCCESS", foundOrder.get().getStatus());
    }

    @Test
    void findById_whenOrderDoesNotExist_shouldReturnEmpty() {
        Optional<Order> foundOrder = orderRepository.findById(999L);
        
        assertFalse(foundOrder.isPresent());
    }

    @Test
    void findAll_whenOrdersExist_shouldReturnAllOrders() {
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        
        List<Order> orders = orderRepository.findAll();
        
        assertEquals(2, orders.size());
        assertTrue(orders.stream().anyMatch(o -> o.getCompanyName().equals("Apple Inc.")));
        assertTrue(orders.stream().anyMatch(o -> o.getCompanyName().equals("Microsoft Corp.")));
    }

    @Test
    void findAll_whenNoOrdersExist_shouldReturnEmptyList() {
        List<Order> orders = orderRepository.findAll();
        
        assertTrue(orders.isEmpty());
    }

    @Test
    void deleteById_shouldRemoveOrder() {
        entityManager.persistAndFlush(order1);
        
        assertTrue(orderRepository.findById(order1.getId()).isPresent());
        
        orderRepository.deleteById(order1.getId());
        
        assertFalse(orderRepository.findById(order1.getId()).isPresent());
    }

    @Test
    void updateOrder_shouldUpdateFields() {
        entityManager.persistAndFlush(order1);
        
        Order orderToUpdate = entityManager.find(Order.class, order1.getId());
        orderToUpdate.setStatus("CANCELLED");
        orderToUpdate.setPrice(160.0);
        
        Order updatedOrder = orderRepository.save(orderToUpdate);
        
        assertEquals("CANCELLED", updatedOrder.getStatus());
        assertEquals(160.0, updatedOrder.getPrice());
        assertEquals(order1.getId(), updatedOrder.getId());
        assertEquals("Apple Inc.", updatedOrder.getCompanyName());
        assertEquals("BUY", updatedOrder.getType());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, orderRepository.count());
        
        entityManager.persistAndFlush(order1);
        assertEquals(1, orderRepository.count());
        
        entityManager.persistAndFlush(order2);
        assertEquals(2, orderRepository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenOrderExists() {
        entityManager.persistAndFlush(order1);
        
        assertTrue(orderRepository.existsById(order1.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenOrderDoesNotExist() {
        assertFalse(orderRepository.existsById(999L));
    }

    @Test
    void saveOrder_withNullCompanyName_shouldPersistCorrectly() {
        Order orderWithNullName = new Order(3L, 3L, 3L, null, "BUY", 25, 300.0, "PENDING");
        Order savedOrder = orderRepository.save(orderWithNullName);

        Order retrievedOrder = entityManager.find(Order.class, savedOrder.getId());
        
        assertNotNull(retrievedOrder);
        assertEquals(3L, retrievedOrder.getUserId());
        assertEquals(3L, retrievedOrder.getPortfolioId());
        assertEquals(3L, retrievedOrder.getCompanyId());
        assertEquals(null, retrievedOrder.getCompanyName());
        assertEquals("BUY", retrievedOrder.getType());
        assertEquals(25, retrievedOrder.getQuantity());
        assertEquals(300.0, retrievedOrder.getPrice());
        assertEquals("PENDING", retrievedOrder.getStatus());
    }

    @Test
    void saveOrder_withZeroQuantity_shouldPersistCorrectly() {
        Order orderWithZeroQuantity = new Order(4L, 4L, 4L, "Test Corp.", "SELL", 0, 100.0, "SUCCESS");
        Order savedOrder = orderRepository.save(orderWithZeroQuantity);

        Order retrievedOrder = entityManager.find(Order.class, savedOrder.getId());
        
        assertNotNull(retrievedOrder);
        assertEquals(4L, retrievedOrder.getUserId());
        assertEquals(4L, retrievedOrder.getPortfolioId());
        assertEquals(4L, retrievedOrder.getCompanyId());
        assertEquals("Test Corp.", retrievedOrder.getCompanyName());
        assertEquals("SELL", retrievedOrder.getType());
        assertEquals(0, retrievedOrder.getQuantity());
        assertEquals(100.0, retrievedOrder.getPrice());
        assertEquals("SUCCESS", retrievedOrder.getStatus());
    }

    @Test
    void saveOrder_withNegativePrice_shouldPersistCorrectly() {
        Order orderWithNegativePrice = new Order(5L, 5L, 5L, "Negative Corp.", "BUY", 10, -50.0, "ERROR");
        Order savedOrder = orderRepository.save(orderWithNegativePrice);

        Order retrievedOrder = entityManager.find(Order.class, savedOrder.getId());
        
        assertNotNull(retrievedOrder);
        assertEquals(5L, retrievedOrder.getUserId());
        assertEquals(5L, retrievedOrder.getPortfolioId());
        assertEquals(5L, retrievedOrder.getCompanyId());
        assertEquals("Negative Corp.", retrievedOrder.getCompanyName());
        assertEquals("BUY", retrievedOrder.getType());
        assertEquals(10, retrievedOrder.getQuantity());
        assertEquals(-50.0, retrievedOrder.getPrice());
        assertEquals("ERROR", retrievedOrder.getStatus());
    }

    @Test
    void saveOrder_withMaxLongValues_shouldPersistCorrectly() {
        Order orderWithMaxValues = new Order(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 
                "Max Corp.", "BUY", Integer.MAX_VALUE, Double.MAX_VALUE, "SUCCESS");
        Order savedOrder = orderRepository.save(orderWithMaxValues);

        Order retrievedOrder = entityManager.find(Order.class, savedOrder.getId());
        
        assertNotNull(retrievedOrder);
        assertEquals(Long.MAX_VALUE, retrievedOrder.getUserId());
        assertEquals(Long.MAX_VALUE, retrievedOrder.getPortfolioId());
        assertEquals(Long.MAX_VALUE, retrievedOrder.getCompanyId());
        assertEquals("Max Corp.", retrievedOrder.getCompanyName());
        assertEquals("BUY", retrievedOrder.getType());
        assertEquals(Integer.MAX_VALUE, retrievedOrder.getQuantity());
        assertEquals(Double.MAX_VALUE, retrievedOrder.getPrice());
        assertEquals("SUCCESS", retrievedOrder.getStatus());
    }
}
