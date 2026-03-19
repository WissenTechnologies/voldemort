package com.example.wallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.wallet.entities.Wallet;
import com.example.wallet.repo.WalletRepository;

@Service
public class WalletService {

    @Autowired
    private WalletRepository repo;

    public Wallet create(Long userId, Double balance) {
        return repo.save(new Wallet(userId, balance));
    }

    public Wallet get(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    // DEBIT
    public void debit(Long userId, Double amount) {

        Wallet w = get(userId);

        if (w.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        w.setBalance(w.getBalance() - amount);
        repo.save(w);
    }

    // CREDIT
    public void credit(Long userId, Double amount) {

        Wallet w = get(userId);

        w.setBalance(w.getBalance() + amount);
        repo.save(w);
    }
    // ADD MONEY
public void addMoney(Long userId, Double amount) {

    Wallet w = get(userId);

    w.setBalance(w.getBalance() + amount);

    repo.save(w);
}

// WITHDRAW MONEY
public void withdraw(Long userId, Double amount) {

    Wallet w = get(userId);

    if (w.getBalance() < amount) {
        throw new RuntimeException("Insufficient balance");
    }

    w.setBalance(w.getBalance() - amount);

    repo.save(w);
}
}
