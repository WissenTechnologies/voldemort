package com.example.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PriceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5002/api/prices";

    public Double getPrice(Long companyId) {
        return restTemplate.getForObject(BASE_URL + "/latest/" + companyId, Double.class);
    }
}
