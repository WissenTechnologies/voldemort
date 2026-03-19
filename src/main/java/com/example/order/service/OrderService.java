package com.example.order.service;

import java.time.LocalDateTime;
import java.util.List;
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

    public Order save(Order order) {
    return repo.save(order);
}

    public boolean isMarketOpen() {
    java.time.LocalTime now = java.time.LocalTime.now();

    java.time.LocalTime open = java.time.LocalTime.of(9, 0);
    java.time.LocalTime close = java.time.LocalTime.of(15, 30);

    return !now.isBefore(open) && !now.isAfter(close);
}

    // ============================
    // BUY ENTRY POINT
    // ============================
    public Order buy(OrderRequest req) {

        validate(req);
        if ("MARKET".equalsIgnoreCase(req.getOrderMode()) && !isMarketOpen()) {
    throw new RuntimeException("Market is closed");
}

        // LIMIT / STOP LOSS → store as PENDING
        if (!"MARKET".equalsIgnoreCase(req.getOrderMode())) {

            Map company = companyClient.getCompany(req.getCompanyId());
            String name = (String) company.get("companyName");

            Order order = new Order(
                    req.getUserId(),
                    req.getPortfolioId(),
                    req.getCompanyId(),
                    name,
                    "BUY",
                    req.getQuantity(),
                    null,
                    "PENDING"
            );

            order.setOrderMode(req.getOrderMode());
            order.setTargetPrice(req.getTargetPrice());

            return repo.save(order);
        }

        // MARKET → instant execution
        return executeBuy(req);
    }

    // ============================
    // SELL ENTRY POINT
    // ============================
    public Order sell(OrderRequest req) {

        validate(req);
        if ("MARKET".equalsIgnoreCase(req.getOrderMode()) && !isMarketOpen()) {
    throw new RuntimeException("Market is closed");
}

        // LIMIT / STOP LOSS → store as PENDING
        if (!"MARKET".equalsIgnoreCase(req.getOrderMode())) {

            Order order = new Order(
                    req.getUserId(),
                    req.getPortfolioId(),
                    req.getCompanyId(),
                    "",
                    "SELL",
                    req.getQuantity(),
                    null,
                    "PENDING"
            );

            order.setOrderMode(req.getOrderMode());
            order.setTargetPrice(req.getTargetPrice());

            return repo.save(order);
        }

        // MARKET → instant execution
        return executeSell(req);
    }

    // ============================
    // CORE BUY LOGIC (REUSED)
    // ============================
    private Order executeBuy(OrderRequest req) {

    Double price = priceClient.getPrice(req.getCompanyId());
    if (price == null) throw new RuntimeException("Price unavailable");

    processBuyTransaction(req, price);

    Map company = companyClient.getCompany(req.getCompanyId());
    String name = (String) company.get("companyName");

    Order order = new Order(
            req.getUserId(),
            req.getPortfolioId(),
            req.getCompanyId(),
            name,
            "BUY",
            req.getQuantity(),
            price,
            "EXECUTED"
    );

    order.setExecutedPrice(price);
    order.setExecutedAt(java.time.LocalDateTime.now());
    order.setOrderMode(req.getOrderMode());
    order.setTargetPrice(price); // optional, for consistency
    return repo.save(order);
}
    // ============================
    // CORE SELL LOGIC (REUSED)
    // ============================
    private Order executeSell(OrderRequest req) {

    Double price = priceClient.getPrice(req.getCompanyId());
    if (price == null) throw new RuntimeException("Price unavailable");

    processSellTransaction(req, price);

    Order order = new Order(
            req.getUserId(),
            req.getPortfolioId(),
            req.getCompanyId(),
            "",
            "SELL",
            req.getQuantity(),
            price,
            "EXECUTED"
    );

    order.setExecutedPrice(price);
    order.setExecutedAt(java.time.LocalDateTime.now());
    order.setOrderMode(req.getOrderMode());
    order.setTargetPrice(price); // optional, for consistency
    return repo.save(order);
}

    // ============================
    // EXECUTION ENGINE METHODS
    // ============================

    public List<Order> getPendingOrders() {
        return repo.findByStatus("PENDING");
    }
   public List<Order> getOrders(Long userId, Long portfolioId, String status) {

    // priority: portfolio > user
    if (portfolioId != null && status != null) {
        return repo.findByPortfolioIdAndStatus(portfolioId, status);
    }

    if (userId != null && status != null) {
        return repo.findByUserIdAndStatus(userId, status);
    }

    if (portfolioId != null) {
        return repo.findByPortfolioId(portfolioId);
    }

    if (userId != null) {
        return repo.findByUserId(userId);
    }

    throw new RuntimeException("Provide userId or portfolioId");
}
    public void executeOrder(Order order, Double currentPrice) {

    if (!"PENDING".equals(order.getStatus())) return;

    try {

        OrderRequest req = new OrderRequest();
        req.setUserId(order.getUserId());
        req.setPortfolioId(order.getPortfolioId());
        req.setCompanyId(order.getCompanyId());
        req.setQuantity(order.getQuantity());

        if ("BUY".equals(order.getType())) {
            processBuyTransaction(req, currentPrice);
        } else {
            processSellTransaction(req, currentPrice);
        }

        // ✅ UPDATE SAME ORDER (NO NEW ENTRY)
        order.setStatus("EXECUTED");
        order.setExecutedPrice(currentPrice);
        order.setExecutedAt(java.time.LocalDateTime.now());

        repo.save(order);

    } catch (Exception e) {
        order.setStatus("FAILED");
        repo.save(order);
    }
}
    public boolean shouldExecute(Order order, Double price) {

        if ("LIMIT".equals(order.getOrderMode())) {

            if ("BUY".equals(order.getType())) {
                return price <= order.getTargetPrice();
            } else {
                return price >= order.getTargetPrice();
            }
        }

        if ("STOP_LOSS".equals(order.getOrderMode())) {
            return price <= order.getTargetPrice();
        }

        return false;
    }

    // ============================
    // HISTORY
    // ============================
    public List<Order> getOrdersByPortfolio(Long portfolioId) {
        return repo.findByPortfolioId(portfolioId);
    }

    // ============================
    // COMMON VALIDATION
    // ============================
    private void validate(OrderRequest req) {

        if (req.getUserId() == null ||
            req.getPortfolioId() == null ||
            req.getCompanyId() == null) {
            throw new RuntimeException("Invalid request");
        }

        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new RuntimeException("Invalid quantity");
        }

        if (!"MARKET".equalsIgnoreCase(req.getOrderMode())) {
            if (req.getTargetPrice() == null) {
                throw new RuntimeException("Target price required");
            }
        }
    }
    private void processBuyTransaction(OrderRequest req, Double price) {

    Map company = companyClient.getCompany(req.getCompanyId());
    String name = (String) company.get("companyName");

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

        walletClient.debit(req.getUserId(), totalCost);
        walletDone = true;

        marketClient.reduce(req.getCompanyId(), qty);
        marketDone = true;

        portfolioClient.buy(
                req.getPortfolioId(),
                req.getCompanyId(),
                name,
                req.getQuantity(),
                price
        );
        portfolioDone = true;

    } catch (Exception e) {

        if (portfolioDone) {
            portfolioClient.sell(req.getPortfolioId(), req.getCompanyId(), req.getQuantity(), price);
        }

        if (marketDone) {
            marketClient.add(req.getCompanyId(), qty);
        }

        if (walletDone) {
            walletClient.credit(req.getUserId(), totalCost);
        }

        throw new RuntimeException("Buy failed: " + e.getMessage());
    }
}

private void processSellTransaction(OrderRequest req, Double price) {

    Double totalValue = price * req.getQuantity();
    Long qty = req.getQuantity().longValue();

    boolean portfolioDone = false;
    boolean marketDone = false;
    boolean walletDone = false;

    try {

        portfolioClient.sell(
                req.getPortfolioId(),
                req.getCompanyId(),
                req.getQuantity(),
                price
        );
        portfolioDone = true;

        marketClient.add(req.getCompanyId(), qty);
        marketDone = true;

        walletClient.credit(req.getUserId(), totalValue);
        walletDone = true;

    } catch (Exception e) {

        if (walletDone) {
            walletClient.debit(req.getUserId(), totalValue);
        }

        if (marketDone) {
            marketClient.reduce(req.getCompanyId(), qty);
        }

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