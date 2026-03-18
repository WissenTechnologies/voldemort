package com.example.demo.Services;

import com.example.demo.Entities.*;
import com.example.demo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepo portfolioRepo;

    @Autowired
    private HoldingRepo holdingRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private StockRepo stockRepo;

    @Autowired
    private CompanyRepo companyRepo;

    public List<Portfolio> getAllPortfolios() {
        return portfolioRepo.findAll();
    }

    public List<Portfolio> getPortfoliosByUser(User user) {
        return portfolioRepo.findByUser(user);
    }

    public Portfolio createPortfolio(Portfolio portfolio) {
        return portfolioRepo.save(portfolio);
    }

    public void deletePortfolio(Long id) {
        portfolioRepo.deleteById(id);
    }

    public Optional<Portfolio> getPortfolioById(Long id) {
        return portfolioRepo.findById(id);
    }

    public Optional<Portfolio> getPortfolioWithHoldingsById(Long id) {
        return portfolioRepo.findWithHoldingsById(id);
    }

    public Optional<Portfolio> getPortfolioWithUserById(Long id) {
        return portfolioRepo.findWithUserById(id);
    }

    private void assertOwnership(Portfolio portfolio, User user) throws Exception {
        if (portfolio.getUser() == null || user == null || user.getId() == null) {
            throw new Exception("Unauthorized");
        }
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new Exception("Forbidden: portfolio does not belong to user");
        }
    }

    @Transactional
    public Portfolio buyStock(Long portfolioId, String symbol, Integer quantity, User user) throws Exception {
        if (quantity == null || quantity <= 0) {
            throw new Exception("Quantity must be greater than 0");
        }

        Portfolio portfolio = portfolioRepo.findWithHoldingsById(portfolioId)
                .orElseThrow(() -> new Exception("Portfolio not found"));
        assertOwnership(portfolio, user);

        Stock stock = stockRepo.findById(symbol)
                .orElseThrow(() -> new Exception("Stock not found"));

        // Company registry ledger (publicly tradable shares)
        Company company = companyRepo.findByTickerIgnoreCase(symbol)
                .orElseThrow(() -> new Exception("Company not found for ticker: " + symbol));

        // Enforce that shares exist in the company registry and are available for public trading.
        Long available = company.getAvailableShares() == null ? 0L : company.getAvailableShares();
        if (available < quantity.longValue()) {
            throw new Exception("Insufficient available shares in market");
        }

        BigDecimal totalCost = stock.getCurrentPrice().multiply(new BigDecimal(quantity));

        if (portfolio.getAvailableCash().compareTo(totalCost) < 0) {
            throw new Exception("Insufficient funds in portfolio");
        }

        // Deduct cash
        portfolio.setAvailableCash(portfolio.getAvailableCash().subtract(totalCost));
        portfolio = portfolioRepo.save(portfolio);

        // Update company share ledger
        company.setAvailableShares(available - quantity.longValue());
        Long outstanding = company.getOutstandingShares() == null ? 0L : company.getOutstandingShares();
        company.setOutstandingShares(outstanding + quantity.longValue());
        companyRepo.save(company);

        // Update holdings
        Optional<Holding> existingHolding = holdingRepo.findByPortfolioAndSymbol(portfolio, symbol);
        if (existingHolding.isPresent()) {
            Holding holding = existingHolding.get();
            BigDecimal oldTotal = holding.getAveragePrice().multiply(new BigDecimal(holding.getQuantity()));
            Integer newQuantity = holding.getQuantity() + quantity;
            BigDecimal newAveragePrice = (oldTotal.add(totalCost)).divide(new BigDecimal(newQuantity), 2, BigDecimal.ROUND_HALF_UP);
            
            holding.setQuantity(newQuantity);
            holding.setAveragePrice(newAveragePrice);
            holdingRepo.save(holding);
        } else {
            Holding newHolding = new Holding(portfolio, symbol, quantity, stock.getCurrentPrice());
            holdingRepo.save(newHolding);
        }

        // Save transaction
        Transaction transaction = new Transaction(user, portfolio, symbol, "BUY", quantity, stock.getCurrentPrice());
        transactionRepo.save(transaction);

        return portfolioRepo.findWithHoldingsById(portfolio.getId()).orElse(portfolio);
    }

    @Transactional
    public Portfolio sellStock(Long portfolioId, String symbol, Integer quantity, User user) throws Exception {
        if (quantity == null || quantity <= 0) {
            throw new Exception("Quantity must be greater than 0");
        }

        Portfolio portfolio = portfolioRepo.findWithHoldingsById(portfolioId)
                .orElseThrow(() -> new Exception("Portfolio not found"));
        assertOwnership(portfolio, user);

        Stock stock = stockRepo.findById(symbol)
                .orElseThrow(() -> new Exception("Stock not found"));

        // Company registry ledger (publicly tradable shares)
        Company company = companyRepo.findByTickerIgnoreCase(symbol)
                .orElseThrow(() -> new Exception("Company not found for ticker: " + symbol));

        Holding holding = holdingRepo.findByPortfolioAndSymbol(portfolio, symbol)
                .orElseThrow(() -> new Exception("No holdings found for symbol: " + symbol));

        if (holding.getQuantity() < quantity) {
            throw new Exception("Insufficient shares offered for sale");
        }

        BigDecimal totalGain = stock.getCurrentPrice().multiply(new BigDecimal(quantity));

        // Add cash
        portfolio.setAvailableCash(portfolio.getAvailableCash().add(totalGain));
        portfolio = portfolioRepo.save(portfolio);

        // Update company share ledger (sold shares return to available pool)
        Long available = company.getAvailableShares() == null ? 0L : company.getAvailableShares();
        Long outstanding = company.getOutstandingShares() == null ? 0L : company.getOutstandingShares();
        if (outstanding < quantity.longValue()) {
            throw new Exception("Company outstanding shares invariant violated");
        }
        company.setAvailableShares(available + quantity.longValue());
        company.setOutstandingShares(outstanding - quantity.longValue());
        companyRepo.save(company);

        // Update holdings
        holding.setQuantity(holding.getQuantity() - quantity);
        if (holding.getQuantity() == 0) {
            holdingRepo.delete(holding);
        } else {
            holdingRepo.save(holding);
        }

        // Save transaction
        Transaction transaction = new Transaction(user, portfolio, symbol, "SELL", quantity, stock.getCurrentPrice());
        transactionRepo.save(transaction);

        return portfolioRepo.findWithHoldingsById(portfolio.getId()).orElse(portfolio);
    }
}
