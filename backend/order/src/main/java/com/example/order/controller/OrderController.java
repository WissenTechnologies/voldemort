package com.example.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.order.dto.OrderRequest;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    private OrderService service;

    // ============================
    // BUY
    // ============================
    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody OrderRequest req) {

        // basic validation
        String validationError = validate(req);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }

        try {
            return ResponseEntity.ok(service.buy(req));

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // ============================
    // SELL
    // ============================
    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody OrderRequest req) {

        // basic validation
        String validationError = validate(req);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }

        try {
            return ResponseEntity.ok(service.sell(req));

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // ============================
    // TRANSACTION HISTORY
    // ============================
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<?> getOrders(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(service.getOrdersByPortfolio(portfolioId));
    }

    // ============================
    // OPTIONAL: GET PENDING ORDERS
    // ============================
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingOrders() {
        return ResponseEntity.ok(service.getPendingOrders());
    }
    @GetMapping("/filter")
public ResponseEntity<?> getOrders(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Long portfolioId,
        @RequestParam(required = false) String status) {

    return ResponseEntity.ok(service.getOrders(userId, portfolioId, status));
}
    // ============================
    // COMMON VALIDATION
    // ============================
    private String validate(OrderRequest req) {

        if (req.getUserId() == null ||
            req.getPortfolioId() == null ||
            req.getCompanyId() == null) {
            return "Missing required fields";
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            return "Invalid quantity";
        }

        // NEW: validate order mode
        if (req.getOrderMode() == null) {
            return "Order mode is required (MARKET / LIMIT / STOP_LOSS)";
        }

        // LIMIT / STOP LOSS must have target price
        if (!"MARKET".equalsIgnoreCase(req.getOrderMode())) {
            if (req.getTargetPrice() == null || req.getTargetPrice() <= 0) {
                return "Target price required for LIMIT / STOP_LOSS";
            }
        }

        return null;
    }

    // ============================
    // CENTRALIZED ERROR HANDLING
    // ============================
    private ResponseEntity<?> handleException(RuntimeException e) {

        String msg = e.getMessage();

        // business errors
        if (msg.contains("Not enough shares") || msg.contains("Insufficient")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
        }

        // not found
        if (msg.contains("not found") || msg.contains("Price unavailable")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }

        // validation
        if (msg.contains("Invalid") || msg.contains("required")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}