package com.example.portfolio_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CompanyServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5001/api/companies";

    public Object getCompany(Long companyId) {
        return restTemplate.getForObject(
                BASE_URL + "/" + companyId,
                Object.class
        );
    }
}