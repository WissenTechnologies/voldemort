package com.example.wallet.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.wallet.entities.Wallet;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    private Wallet wallet1;
    private Wallet wallet2;
    private Wallet wallet3;

    @BeforeEach
    void setUp() {
        wallet1 = new Wallet(1L, 1000.0);
        wallet2 = new Wallet(2L, 2000.0);
        wallet3 = new Wallet(3L, 500.0);
    }

    @Test
    void saveWallet_shouldPersistWallet() {
        Wallet savedWallet = walletRepository.save(wallet1);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(savedWallet.getUserId(), retrievedWallet.getUserId());
        assertEquals(1000.0, retrievedWallet.getBalance());
    }

    @Test
    void findById_whenWalletExists_shouldReturnWallet() {
        entityManager.persistAndFlush(wallet1);
        
        Optional<Wallet> foundWallet = walletRepository.findById(wallet1.getUserId());
        
        assertTrue(foundWallet.isPresent());
        assertEquals(wallet1.getUserId(), foundWallet.get().getUserId());
        assertEquals(1000.0, foundWallet.get().getBalance());
    }

    @Test
    void findById_whenWalletDoesNotExist_shouldReturnEmpty() {
        Optional<Wallet> foundWallet = walletRepository.findById(999L);
        
        assertFalse(foundWallet.isPresent());
    }

    @Test
    void findAll_whenWalletsExist_shouldReturnAllWallets() {
        entityManager.persistAndFlush(wallet1);
        entityManager.persistAndFlush(wallet2);
        entityManager.persistAndFlush(wallet3);
        
        List<Wallet> wallets = walletRepository.findAll();
        
        assertEquals(3, wallets.size());
        assertTrue(wallets.stream().anyMatch(w -> w.getUserId().equals(1L) && w.getBalance().equals(1000.0)));
        assertTrue(wallets.stream().anyMatch(w -> w.getUserId().equals(2L) && w.getBalance().equals(2000.0)));
        assertTrue(wallets.stream().anyMatch(w -> w.getUserId().equals(3L) && w.getBalance().equals(500.0)));
    }

    @Test
    void findAll_whenNoWalletsExist_shouldReturnEmptyList() {
        List<Wallet> wallets = walletRepository.findAll();
        
        assertTrue(wallets.isEmpty());
    }

    @Test
    void deleteById_shouldRemoveWallet() {
        entityManager.persistAndFlush(wallet1);
        
        assertTrue(walletRepository.findById(wallet1.getUserId()).isPresent());
        
        walletRepository.deleteById(wallet1.getUserId());
        
        assertFalse(walletRepository.findById(wallet1.getUserId()).isPresent());
    }

    @Test
    void updateWallet_shouldUpdateBalance() {
        entityManager.persistAndFlush(wallet1);
        
        Wallet walletToUpdate = entityManager.find(Wallet.class, wallet1.getUserId());
        walletToUpdate.setBalance(1500.0);
        
        Wallet updatedWallet = walletRepository.save(walletToUpdate);
        
        assertEquals(1500.0, updatedWallet.getBalance());
        assertEquals(wallet1.getUserId(), updatedWallet.getUserId());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, walletRepository.count());
        
        entityManager.persistAndFlush(wallet1);
        assertEquals(1, walletRepository.count());
        
        entityManager.persistAndFlush(wallet2);
        assertEquals(2, walletRepository.count());
        
        entityManager.persistAndFlush(wallet3);
        assertEquals(3, walletRepository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenWalletExists() {
        entityManager.persistAndFlush(wallet1);
        
        assertTrue(walletRepository.existsById(wallet1.getUserId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenWalletDoesNotExist() {
        assertFalse(walletRepository.existsById(999L));
    }

    @Test
    void saveWallet_withZeroBalance_shouldPersistCorrectly() {
        Wallet walletWithZeroBalance = new Wallet(4L, 0.0);
        Wallet savedWallet = walletRepository.save(walletWithZeroBalance);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(4L, retrievedWallet.getUserId());
        assertEquals(0.0, retrievedWallet.getBalance());
    }

    @Test
    void saveWallet_withNegativeBalance_shouldPersistCorrectly() {
        Wallet walletWithNegativeBalance = new Wallet(5L, -100.0);
        Wallet savedWallet = walletRepository.save(walletWithNegativeBalance);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(5L, retrievedWallet.getUserId());
        assertEquals(-100.0, retrievedWallet.getBalance());
    }

    @Test
    void saveWallet_withMaxLongUserId_shouldPersistCorrectly() {
        Wallet walletWithMaxLongUserId = new Wallet(Long.MAX_VALUE, 999999.99);
        Wallet savedWallet = walletRepository.save(walletWithMaxLongUserId);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(Long.MAX_VALUE, retrievedWallet.getUserId());
        assertEquals(999999.99, retrievedWallet.getBalance());
    }

    @Test
    void saveWallet_withMaxDoubleBalance_shouldPersistCorrectly() {
        Wallet walletWithMaxBalance = new Wallet(6L, Double.MAX_VALUE);
        Wallet savedWallet = walletRepository.save(walletWithMaxBalance);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(6L, retrievedWallet.getUserId());
        assertEquals(Double.MAX_VALUE, retrievedWallet.getBalance());
    }

    @Test
    void saveWallet_withMinDoubleBalance_shouldPersistCorrectly() {
        Wallet walletWithMinBalance = new Wallet(7L, Double.MIN_VALUE);
        Wallet savedWallet = walletRepository.save(walletWithMinBalance);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(7L, retrievedWallet.getUserId());
        assertEquals(Double.MIN_VALUE, retrievedWallet.getBalance());
    }

    @Test
    void saveWallet_withDecimalBalance_shouldPersistCorrectly() {
        Wallet walletWithDecimalBalance = new Wallet(8L, 1234.56);
        Wallet savedWallet = walletRepository.save(walletWithDecimalBalance);

        Wallet retrievedWallet = entityManager.find(Wallet.class, savedWallet.getUserId());
        
        assertNotNull(retrievedWallet);
        assertEquals(8L, retrievedWallet.getUserId());
        assertEquals(1234.56, retrievedWallet.getBalance(), 0.001);
    }

    @Test
    void updateWallet_withZeroBalance_shouldUpdateCorrectly() {
        entityManager.persistAndFlush(wallet1);
        
        Wallet walletToUpdate = entityManager.find(Wallet.class, wallet1.getUserId());
        walletToUpdate.setBalance(0.0);
        
        Wallet updatedWallet = walletRepository.save(walletToUpdate);
        
        assertEquals(0.0, updatedWallet.getBalance());
        assertEquals(wallet1.getUserId(), updatedWallet.getUserId());
    }

    @Test
    void updateWallet_withNegativeBalance_shouldUpdateCorrectly() {
        entityManager.persistAndFlush(wallet1);
        
        Wallet walletToUpdate = entityManager.find(Wallet.class, wallet1.getUserId());
        walletToUpdate.setBalance(-500.0);
        
        Wallet updatedWallet = walletRepository.save(walletToUpdate);
        
        assertEquals(-500.0, updatedWallet.getBalance());
        assertEquals(wallet1.getUserId(), updatedWallet.getUserId());
    }
}
