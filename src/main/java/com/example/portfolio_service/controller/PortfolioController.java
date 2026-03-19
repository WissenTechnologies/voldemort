package com.example.portfolio_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.portfolio_service.dto.CommonResponse;
import com.example.portfolio_service.dto.HoldingRequest;
import com.example.portfolio_service.dto.PortfolioResponse;
import com.example.portfolio_service.entites.Portfolio;
import com.example.portfolio_service.services.PortfolioService;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin("*")
public class PortfolioController {

    @Autowired
    private PortfolioService service;

    // ✅ CREATE PORTFOLIO
    @PostMapping
    public ResponseEntity<CommonResponse> createPortfolio(
            @RequestParam Long userId,
            @RequestParam String name) {

        // 🔴 VALIDATIONS
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse(false, "Invalid userId", null));
        }

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse(false, "Portfolio name is required", null));
        }

        try {
            Portfolio portfolio = service.createPortfolio(userId, name);

            return ResponseEntity.ok(
                    new CommonResponse(true, "Portfolio created successfully", portfolio)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new CommonResponse(false, "Failed to create portfolio", null));
        }
    }

    // ✅ GET PORTFOLIO
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse> getPortfolio(@PathVariable Long userId) {

        // 🔴 VALIDATION
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse(false, "Invalid userId", null));
        }

        try {
            PortfolioResponse response = service.getPortfolio(userId);

            // Optional: if no data
            if (response.getHoldings().isEmpty()) {
                return ResponseEntity.ok(
                        new CommonResponse(true, "No holdings found", response)
                );
            }

            return ResponseEntity.ok(
                    new CommonResponse(true, "Portfolio fetched successfully", response)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new CommonResponse(false, "Error fetching portfolio", null));
        }
        
    }
    @PostMapping("/buy")
public ResponseEntity<CommonResponse> buy(@RequestBody HoldingRequest request) {

    if (request.getQuantity() == null || request.getQuantity() <= 0) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse(false, "Invalid quantity", null));
    }

    service.buyHolding(request);

    return ResponseEntity.ok(
            new CommonResponse(true, "Stock bought successfully", null)
    );
}
@PostMapping("/sell")
public ResponseEntity<CommonResponse> sell(@RequestBody HoldingRequest request) {

    if (request.getQuantity() == null || request.getQuantity() <= 0) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse(false, "Invalid quantity", null));
    }

    try {
        service.sellHolding(request);

        return ResponseEntity.ok(
                new CommonResponse(true, "Stock sold successfully", null)
        );

    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse(false, e.getMessage(), null));
    }
}
}
