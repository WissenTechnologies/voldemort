package com.example.market.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CompanyClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5001/api/companies";

    public Map getCompany(Long companyId) {
        return restTemplate.getForObject(BASE_URL + "/" + companyId, Map.class);
    }
}