package com.example.market.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.market.service.MarketService;

@RestController
@RequestMapping("/api/market")
@CrossOrigin("*")
public class MarketController {

    @Autowired
    private MarketService service;

    // INIT
    @PostMapping("/init/{companyId}")
    public ResponseEntity<?> init(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.initialize(companyId));
    }

    // GET
    @GetMapping("/{companyId}")
    public ResponseEntity<?> get(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.get(companyId));
    }

    // BUY
    @PostMapping("/buy")
    public ResponseEntity<?> buy(
            @RequestParam Long companyId,
            @RequestParam Long qty) {

        service.reduceShares(companyId, qty);
        return ResponseEntity.ok("Shares deducted");
    }

    // SELL
    @PostMapping("/sell")
    public ResponseEntity<?> sell(
            @RequestParam Long companyId,
            @RequestParam Long qty) {

        service.addShares(companyId, qty);
        return ResponseEntity.ok("Shares added");
    }
}