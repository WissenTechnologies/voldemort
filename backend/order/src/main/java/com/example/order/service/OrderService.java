package com.example.order.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.order.client.CompanyClient;
import com.example.order.client.MarketClient;
import com.example.order.client.PortfolioClient;
import com.example.order.client.PriceClient;
import com.example.order.client.WalletClient;
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
    @Autowired private WalletClient walletClient;

    // BUY (TRANSACTION SAFE)
    public Order buy(OrderRequest req) {

        // basic validation
        if (req.getUserId() == null || req.getPortfolioId() == null || req.getCompanyId() == null) {
            throw new RuntimeException("Invalid request");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new RuntimeException("Invalid quantity");
        }

        // get company
        Map company = companyClient.getCompany(req.getCompanyId());
        String name = (String) company.get("companyName");

        // get price
        Double price = priceClient.getPrice(req.getCompanyId());
        if (price == null) {
            throw new RuntimeException("Price unavailable");
        }

        // check market shares
        Long available = marketClient.getAvailableShares(req.getCompanyId());
        if (available == null || available < req.getQuantity()) {
            throw new RuntimeException("Not enough shares");
        }

        Double totalCost = price * req.getQuantity();
        Long qty = req.getQuantity().longValue();

        boolean walletDone = false;
        boolean marketDone = false;
        boolean portfolioDone = false;

        try {

            // step 1: debit wallet
            walletClient.debit(req.getUserId(), totalCost);
            walletDone = true;

            // step 2: reduce market shares
            marketClient.reduce(req.getCompanyId(), qty);
            marketDone = true;

            // step 3: update portfolio
            portfolioClient.buy(
                    req.getPortfolioId(),
                    req.getCompanyId(),
                    name,
                    req.getQuantity(),
                    price
            );
            portfolioDone = true;

            // step 4: save order
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

        } catch (Exception e) {

            // rollback logic

            // rollback portfolio (reverse buy)
            if (portfolioDone) {
                portfolioClient.sell(
                        req.getPortfolioId(),
                        req.getCompanyId(),
                        req.getQuantity(),
                        price
                );
            }

            // rollback market (add shares back)
            if (marketDone) {
                marketClient.add(req.getCompanyId(), qty);
            }

            // rollback wallet (refund)
            if (walletDone) {
                walletClient.credit(req.getUserId(), totalCost);
            }

            throw new RuntimeException("Buy failed: " + e.getMessage());
        }
    }

    // SELL (TRANSACTION SAFE)
    public Order sell(OrderRequest req) {

        // basic validation
        if (req.getUserId() == null || req.getPortfolioId() == null || req.getCompanyId() == null) {
            throw new RuntimeException("Invalid request");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new RuntimeException("Invalid quantity");
        }

        // get price
        Double price = priceClient.getPrice(req.getCompanyId());
        if (price == null) {
            throw new RuntimeException("Price unavailable");
        }

        Double totalValue = price * req.getQuantity();
        Long qty = req.getQuantity().longValue();

        boolean portfolioDone = false;
        boolean marketDone = false;
        boolean walletDone = false;

        try {

            // step 1: update portfolio
            portfolioClient.sell(
                    req.getPortfolioId(),
                    req.getCompanyId(),
                    req.getQuantity(),
                    price
            );
            portfolioDone = true;

            // step 2: add shares to market
            marketClient.add(req.getCompanyId(), qty);
            marketDone = true;

            // step 3: credit wallet
            walletClient.credit(req.getUserId(), totalValue);
            walletDone = true;

            // step 4: save order
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

        } catch (Exception e) {

            // rollback logic

            // rollback wallet (reverse credit)
            if (walletDone) {
                walletClient.debit(req.getUserId(), totalValue);
            }

            // rollback market (remove added shares)
            if (marketDone) {
                marketClient.reduce(req.getCompanyId(), qty);
            }

            // rollback portfolio (restore shares)
            if (portfolioDone) {
                Map company = companyClient.getCompany(req.getCompanyId());
                String name = (String) company.get("companyName");

                portfolioClient.buy(
                        req.getPortfolioId(),
                        req.getCompanyId(),
                        name,
                        req.getQuantity(),
                        price
                );
            }

            throw new RuntimeException("Sell failed: " + e.getMessage());
        }
    }
}