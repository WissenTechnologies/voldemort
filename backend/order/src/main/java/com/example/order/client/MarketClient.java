package com.example.order.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MarketClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5009/api/market";

    public Long getAvailableShares(Long companyId) {
        Map res = restTemplate.getForObject(BASE_URL + "/" + companyId, Map.class);
        return ((Number) res.get("availableShares")).longValue();
    }

    public void reduce(Long companyId, Long qty) {
        restTemplate.postForObject(BASE_URL + "/buy?companyId=" + companyId + "&qty=" + qty, null, String.class);
    }

    public void add(Long companyId, Long qty) {
        restTemplate.postForObject(BASE_URL + "/sell?companyId=" + companyId + "&qty=" + qty, null, String.class);
    }
}
