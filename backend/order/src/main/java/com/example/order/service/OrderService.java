package com.example.order.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.order.client.CompanyClient;
import com.example.order.client.MarketClient;
import com.example.order.client.PortfolioClient;
import com.example.order.client.PriceClient;
import com.example.order.dto.OrderRequest;
import com.example.order.entities.Order;
import com.example.order.repo.OrderRepository;

@Service
public class OrderService {

    @Autowired private OrderRepository repo;
    @Autowired private CompanyClient companyClient;
    @Autowired private PriceClient priceClient;
    @Autowired private MarketClient marketClient;
    @Autowired private PortfolioClient portfolioClient;

    // BUY
    public Order buy(OrderRequest req) {

        Map company = companyClient.getCompany(req.getCompanyId());
        String name = (String) company.get("companyName");

        Double price = priceClient.getPrice(req.getCompanyId());

        Long available = marketClient.getAvailableShares(req.getCompanyId());

        if (available < req.getQuantity()) {
            throw new RuntimeException("Not enough shares");
        }

        // update market
        marketClient.reduce(req.getCompanyId(), req.getQuantity().longValue());

        // update portfolio
        portfolioClient.buy(req.getPortfolioId(), req.getCompanyId(),
                name, req.getQuantity(), price);

        Order order = new Order(
                req.getUserId(),
                req.getPortfolioId(),
                req.getCompanyId(),
                name,
                "BUY",
                req.getQuantity(),
                price,
                "SUCCESS"
        );

        return repo.save(order);
    }

    // SELL
    public Order sell(OrderRequest req) {

        Double price = priceClient.getPrice(req.getCompanyId());

        // update portfolio first
        portfolioClient.sell(req.getPortfolioId(),
                req.getCompanyId(),
                req.getQuantity(),
                price);

        // update market
        marketClient.add(req.getCompanyId(), req.getQuantity().longValue());

        Order order = new Order(
                req.getUserId(),
                req.getPortfolioId(),
                req.getCompanyId(),
                "",
                "SELL",
                req.getQuantity(),
                price,
                "SUCCESS"
        );

        return repo.save(order);
    }
}
