package com.example.company.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MarketClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MarketClient(RestTemplate restTemplate,
            @Value("${market.service.base-url:http://localhost:5009}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void initMarket(long companyId) {
        restTemplate.postForEntity(baseUrl + "/api/market/init/" + companyId, null, Object.class);
    }
}
