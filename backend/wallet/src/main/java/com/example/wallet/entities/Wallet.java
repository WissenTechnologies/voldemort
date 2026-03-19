package com.example.wallet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Wallet {

    @Id
    private Long userId;

    private Double balance;

    public Wallet() {}

    public Wallet(Long userId, Double balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
