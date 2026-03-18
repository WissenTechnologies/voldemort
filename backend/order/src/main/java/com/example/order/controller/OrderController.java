package com.example.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.order.dto.OrderRequest;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody OrderRequest req) {

        if (req.getUserId() == null || req.getUserId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid userId");
        }

        if (req.getPortfolioId() == null || req.getPortfolioId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid portfolioId");
        }

        if (req.getCompanyId() == null || req.getCompanyId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid companyId");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }

        try {
            return ResponseEntity.ok(service.buy(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            // Downstream validation failures (e.g. not enough shares) should be readable to client
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Buy failed: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody OrderRequest req) {

        if (req.getUserId() == null || req.getUserId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid userId");
        }

        if (req.getPortfolioId() == null || req.getPortfolioId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid portfolioId");
        }

        if (req.getCompanyId() == null || req.getCompanyId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid companyId");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }

        try {
            return ResponseEntity.ok(service.sell(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sell failed: " + e.getMessage());
        }
    }
}
