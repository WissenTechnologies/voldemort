package com.example.wallet.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.wallet.services.WalletService;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin("*")
public class WalletController {

    @Autowired
    private WalletService service;

    // CREATE WALLET
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestParam Long userId,
            @RequestParam Double balance) {

        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid userId"));
        }

        if (balance == null || balance < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid balance"));
        }

        return ResponseEntity.ok(service.create(userId, balance));
    }

    // GET WALLET
    @GetMapping("/{userId}")
    public ResponseEntity<?> get(@PathVariable Long userId) {

        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid userId"));
        }

        return ResponseEntity.ok(service.get(userId));
    }

    // ADD MONEY (DEPOSIT)
    @PostMapping("/add")
    public ResponseEntity<?> addMoney(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid userId"));
        }

        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid amount"));
        }

        service.addMoney(userId, amount);

        return ResponseEntity.ok(
                Map.of("message", "Money added successfully")
        );
    }

    // WITHDRAW MONEY
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid userId"));
        }

        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid amount"));
        }

        try {
            service.withdraw(userId, amount);

            return ResponseEntity.ok(
                    Map.of("message", "Money withdrawn successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // INTERNAL: DEBIT (BUY)
    @PostMapping("/debit")
    public ResponseEntity<?> debit(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        try {
            service.debit(userId, amount);

            return ResponseEntity.ok(
                    Map.of("message", "Amount debited")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // INTERNAL: CREDIT (SELL)
    @PostMapping("/credit")
    public ResponseEntity<?> credit(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        service.credit(userId, amount);

        return ResponseEntity.ok(
                Map.of("message", "Amount credited")
        );
    }
}