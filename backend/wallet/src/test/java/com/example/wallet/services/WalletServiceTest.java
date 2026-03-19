package com.example.wallet.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.wallet.entities.Wallet;
import com.example.wallet.repo.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet existingWallet;

    @BeforeEach
    void setUp() {
        existingWallet = new Wallet(1L, 1000.0);
    }

    @Test
    void create_shouldSaveAndReturnWallet() {
        Wallet wallet = new Wallet(1L, 500.0);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet result = walletService.create(1L, 500.0);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(500.0, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void get_whenWalletExists_returnsWallet() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));

        Wallet result = walletService.get(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1000.0, result.getBalance());
        verify(walletRepository).findById(1L);
    }

    @Test
    void get_whenWalletDoesNotExist_throwsRuntimeException() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.get(999L));
        verify(walletRepository).findById(999L);
    }

    @Test
    void debit_whenSufficientBalance_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.debit(1L, 300.0);

        assertEquals(700.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void debit_whenInsufficientBalance_throwsRuntimeException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));

        assertThrows(RuntimeException.class, () -> walletService.debit(1L, 1500.0));

        assertEquals(1000.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void debit_whenExactBalance_setsBalanceToZero() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.debit(1L, 1000.0);

        assertEquals(0.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void credit_whenWalletExists_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.credit(1L, 500.0);

        assertEquals(1500.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void addMoney_whenWalletExists_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.addMoney(1L, 200.0);

        assertEquals(1200.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void addMoney_whenWalletDoesNotExist_createsNewWallet() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.addMoney(1L, 300.0);

        verify(walletRepository).findById(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_whenSufficientBalance_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.withdraw(1L, 400.0);

        assertEquals(600.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void withdraw_whenInsufficientBalance_throwsRuntimeException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));

        assertThrows(RuntimeException.class, () -> walletService.withdraw(1L, 2000.0));

        assertEquals(1000.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void debit_withZeroAmount_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.debit(1L, 0.0);

        assertEquals(1000.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void credit_withZeroAmount_updatesBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.credit(1L, 0.0);

        assertEquals(1000.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void addMoney_withNegativeAmount_createsNewWallet() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.addMoney(2L, -100.0);

        verify(walletRepository).findById(2L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_withExactBalance_setsBalanceToZero() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.withdraw(1L, 1000.0);

        assertEquals(0.0, existingWallet.getBalance());
        verify(walletRepository).findById(1L);
        verify(walletRepository).save(existingWallet);
    }
}
