package com.example.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WalletClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5003/api/wallet";

    // DEBIT (BUY)
    public void debit(Long userId, Double amount) {
        restTemplate.postForObject(
                BASE_URL + "/debit?userId=" + userId + "&amount=" + amount,
                null,
                Object.class
        );
    }

    // CREDIT (SELL)
    public void credit(Long userId, Double amount) {
        restTemplate.postForObject(
                BASE_URL + "/credit?userId=" + userId + "&amount=" + amount,
                null,
                Object.class
        );
    }

    // rollback for debit
public void refund(Long userId, Double amount) {
    credit(userId, amount);
}

// rollback for credit
public void reverseCredit(Long userId, Double amount) {
    debit(userId, amount);
}
}
