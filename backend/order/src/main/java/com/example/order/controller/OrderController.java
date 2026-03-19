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

    // BUY
    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody OrderRequest req) {

        // validation
        if (req.getUserId() == null || req.getPortfolioId() == null || req.getCompanyId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required fields");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid quantity");
        }

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.buy(req));

        } catch (RuntimeException e) {

            String msg = e.getMessage();

            // business errors
            if (msg.contains("Not enough shares") || msg.contains("Insufficient")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
            }

            // not found cases
            if (msg.contains("not found") || msg.contains("Price unavailable")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }

            // fallback
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(msg);
        }
    }

    // SELL
    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody OrderRequest req) {

        // validation
        if (req.getUserId() == null || req.getPortfolioId() == null || req.getCompanyId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required fields");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid quantity");
        }

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.sell(req));

        } catch (RuntimeException e) {

            String msg = e.getMessage();

            // business errors
            if (msg.contains("Insufficient") || msg.contains("not enough")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
            }

            // not found
            if (msg.contains("not found") || msg.contains("Price unavailable")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }

            // fallback
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(msg);
        }
    }
}