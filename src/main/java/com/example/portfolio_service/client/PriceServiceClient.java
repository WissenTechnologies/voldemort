package com.example.portfolio_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PriceServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String PRICE_SERVICE_URL = "http://localhost:5002/api/prices";

    public Double getLatestPrice(Long companyId) {
        return restTemplate.getForObject(
                PRICE_SERVICE_URL + "/latest/" + companyId,
                Double.class
        );
    }
}
