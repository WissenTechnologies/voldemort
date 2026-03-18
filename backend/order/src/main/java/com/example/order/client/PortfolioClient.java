package com.example.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.order.dto.PortfolioRequest;

@Component
public class PortfolioClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:5007/api/portfolio";

    public void buy(Long portfolioId, Long companyId, String name, int qty, double price) {

        PortfolioRequest req = new PortfolioRequest();
        req.setPortfolioId(portfolioId);
        req.setCompanyId(companyId);
        req.setCompanyName(name);
        req.setQuantity(qty);
        req.setPrice(price);

        restTemplate.postForObject(BASE_URL + "/buy", req, Object.class);
    }

    public void sell(Long portfolioId, Long companyId, int qty, double price) {

        PortfolioRequest req = new PortfolioRequest();
        req.setPortfolioId(portfolioId);
        req.setCompanyId(companyId);
        req.setQuantity(qty);
        req.setPrice(price);

        restTemplate.postForObject(BASE_URL + "/sell", req, Object.class);
    }
}