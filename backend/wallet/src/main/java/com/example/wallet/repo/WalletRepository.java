package com.example.wallet.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.wallet.entities.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {}
