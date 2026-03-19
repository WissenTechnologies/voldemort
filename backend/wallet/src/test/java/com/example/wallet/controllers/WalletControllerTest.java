package com.example.wallet.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.wallet.entities.Wallet;
import com.example.wallet.services.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @Test
    void createWallet_whenValidRequest_returnsWallet() throws Exception {
        Wallet wallet = new Wallet(1L, 1000.0);
        when(walletService.create(anyLong(), anyDouble())).thenReturn(wallet);

        mockMvc.perform(post("/api/wallet/create")
                        .param("userId", "1")
                        .param("balance", "1000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.0));

        verify(walletService).create(1L, 1000.0);
    }

    @Test
    void createWallet_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/create")
                        .param("userId", "0")
                        .param("balance", "1000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid userId"));
    }

    @Test
    void createWallet_whenNullUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/create")
                        .param("balance", "1000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid userId"));
    }

    @Test
    void createWallet_whenNegativeBalance_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/create")
                        .param("userId", "1")
                        .param("balance", "-100.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid balance"));
    }

    @Test
    void createWallet_whenNullBalance_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/create")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid balance"));
    }

    @Test
    void getWallet_whenValidUserId_returnsWallet() throws Exception {
        Wallet wallet = new Wallet(1L, 1500.0);
        when(walletService.get(anyLong())).thenReturn(wallet);

        mockMvc.perform(get("/api/wallet/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1500.0));

        verify(walletService).get(1L);
    }

    @Test
    void getWallet_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/wallet/{userId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid userId"));
    }

    @Test
    void getWallet_whenWalletNotFound_returnsBadRequest() throws Exception {
        when(walletService.get(anyLong())).thenThrow(new RuntimeException("Wallet not found"));

        mockMvc.perform(get("/api/wallet/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Wallet not found"));
    }

    @Test
    void addMoney_whenValidRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "1")
                        .param("amount", "500.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Money added successfully"));

        verify(walletService).addMoney(1L, 500.0);
    }

    @Test
    void addMoney_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "-1")
                        .param("amount", "500.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid userId"));
    }

    @Test
    void addMoney_whenNegativeAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "1")
                        .param("amount", "-100.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid amount"));
    }

    @Test
    void addMoney_whenZeroAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "1")
                        .param("amount", "0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid amount"));
    }

    @Test
    void addMoney_whenServiceThrows_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Service error")).when(walletService).addMoney(anyLong(), anyDouble());

        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "1")
                        .param("amount", "500.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Service error"));
    }

    @Test
    void withdraw_whenValidRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/withdraw")
                        .param("userId", "1")
                        .param("amount", "200.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Money withdrawn successfully"));

        verify(walletService).withdraw(1L, 200.0);
    }

    @Test
    void withdraw_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/withdraw")
                        .param("userId", "null")
                        .param("amount", "200.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid userId"));
    }

    @Test
    void withdraw_whenNegativeAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/withdraw")
                        .param("userId", "1")
                        .param("amount", "-50.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid amount"));
    }

    @Test
    void withdraw_whenInsufficientBalance_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Insufficient balance")).when(walletService).withdraw(anyLong(), anyDouble());

        mockMvc.perform(post("/api/wallet/withdraw")
                        .param("userId", "1")
                        .param("amount", "1000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void debit_whenValidRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/debit")
                        .param("userId", "1")
                        .param("amount", "300.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Amount debited"));

        verify(walletService).debit(1L, 300.0);
    }

    @Test
    void debit_whenInsufficientBalance_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Insufficient balance")).when(walletService).debit(anyLong(), anyDouble());

        mockMvc.perform(post("/api/wallet/debit")
                        .param("userId", "1")
                        .param("amount", "5000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void credit_whenValidRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/credit")
                        .param("userId", "1")
                        .param("amount", "750.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Amount credited"));

        verify(walletService).credit(1L, 750.0);
    }

    @Test
    void credit_whenWalletNotFound_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Wallet not found")).when(walletService).credit(anyLong(), anyDouble());

        mockMvc.perform(post("/api/wallet/credit")
                        .param("userId", "999")
                        .param("amount", "100.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Wallet not found"));
    }

    @Test
    void createWallet_whenZeroBalance_createsWalletSuccessfully() throws Exception {
        Wallet wallet = new Wallet(1L, 0.0);
        when(walletService.create(anyLong(), anyDouble())).thenReturn(wallet);

        mockMvc.perform(post("/api/wallet/create")
                        .param("userId", "1")
                        .param("balance", "0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void addMoney_withDecimalAmount_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/add")
                        .param("userId", "1")
                        .param("amount", "123.45")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Money added successfully"));

        verify(walletService).addMoney(1L, 123.45);
    }

    @Test
    void withdraw_withExactBalance_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/wallet/withdraw")
                        .param("userId", "1")
                        .param("amount", "1000.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Money withdrawn successfully"));

        verify(walletService).withdraw(1L, 1000.0);
    }
}
