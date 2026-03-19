package com.example.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class WalletClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5003/api/wallet";

    // DEBIT (BUY)
    public void debit(Long userId, Double amount) {
        try {
            restTemplate.postForObject(
                    BASE_URL + "/debit?userId=" + userId + "&amount=" + amount,
                    null,
                    Object.class
            );
        } catch (RestClientResponseException e) {
            throw new RuntimeException(extractErrorMessage(e), e);
        }
    }

    // CREDIT (SELL)
    public void credit(Long userId, Double amount) {
        try {
            restTemplate.postForObject(
                    BASE_URL + "/credit?userId=" + userId + "&amount=" + amount,
                    null,
                    Object.class
            );
        } catch (RestClientResponseException e) {
            throw new RuntimeException(extractErrorMessage(e), e);
        }
    }

    private String extractErrorMessage(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return e.getStatusText();
        }

        String trimmed = body.trim();
        int idx = trimmed.indexOf("\"error\"");
        if (idx >= 0) {
            int colon = trimmed.indexOf(':', idx);
            if (colon >= 0) {
                int firstQuote = trimmed.indexOf('"', colon + 1);
                if (firstQuote >= 0) {
                    int secondQuote = trimmed.indexOf('"', firstQuote + 1);
                    if (secondQuote > firstQuote) {
                        return trimmed.substring(firstQuote + 1, secondQuote);
                    }
                }
            }
        }

        return trimmed;
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
