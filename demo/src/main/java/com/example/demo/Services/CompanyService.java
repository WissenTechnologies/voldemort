package com.example.demo.Services;

import com.example.demo.Entities.Company;
import com.example.demo.Entities.Stock;
import com.example.demo.Repository.CompanyRepo;
import com.example.demo.Repository.StockRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private StockRepo stockRepo;

    public List<Company> getAll() {
        return companyRepo.findAll();
    }

    public Optional<Company> getById(Long id) {
        return companyRepo.findById(id);
    }

    @Transactional
    public Company create(Company company) throws Exception {
        if (company.getTicker() == null || company.getTicker().trim().isEmpty()) {
            throw new Exception("Ticker is required");
        }
        if (companyRepo.findByTickerIgnoreCase(company.getTicker().trim()).isPresent()) {
            throw new Exception("Ticker already exists");
        }

        if (company.getPrice() == null) {
            company.setPrice(BigDecimal.ZERO);
        }
        if (company.getMarketCap() == null) {
            company.setMarketCap("-");
        }

        Company saved = companyRepo.save(company);
        syncToStock(saved);
        return saved;
    }

    @Transactional
    public Company update(Long id, Company patch) throws Exception {
        Company existing = companyRepo.findById(id)
            .orElseThrow(() -> new Exception("Company not found"));

        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getSector() != null) existing.setSector(patch.getSector());
        if (patch.getWebsite() != null) existing.setWebsite(patch.getWebsite());
        if (patch.getLogoUrl() != null) existing.setLogoUrl(patch.getLogoUrl());
        if (patch.getMarketCap() != null) existing.setMarketCap(patch.getMarketCap());
        if (patch.getPrice() != null) existing.setPrice(patch.getPrice());

        // Disallow ticker changes for now (keeps stock symbol consistent)

        Company saved = companyRepo.save(existing);
        syncToStock(saved);
        return saved;
    }

    @Transactional
    public void delete(Long id) throws Exception {
        Company existing = companyRepo.findById(id)
            .orElseThrow(() -> new Exception("Company not found"));

        // Also delete stock listing if present
        stockRepo.findById(existing.getTicker()).ifPresent(stockRepo::delete);
        companyRepo.delete(existing);
    }

    @Transactional
    public Company issueShares(Long id, Long quantity) throws Exception {
        if (quantity == null || quantity <= 0) {
            throw new Exception("Quantity must be greater than 0");
        }

        Company existing = companyRepo.findById(id)
            .orElseThrow(() -> new Exception("Company not found"));

        existing.setTotalShares((existing.getTotalShares() == null ? 0L : existing.getTotalShares()) + quantity);
        existing.setAvailableShares((existing.getAvailableShares() == null ? 0L : existing.getAvailableShares()) + quantity);

        return companyRepo.save(existing);
    }

    private void syncToStock(Company company) {
        final String symbol = company.getTicker().trim().toUpperCase();
        Stock stock = stockRepo.findById(symbol).orElse(new Stock());

        stock.setSymbol(symbol);
        stock.setName(company.getName() == null ? symbol : company.getName());

        BigDecimal price = company.getPrice() == null ? BigDecimal.ZERO : company.getPrice();
        stock.setCurrentPrice(price);
        stock.setDayHigh(price);
        stock.setDayLow(price);

        if (company.getMarketCap() != null) {
            stock.setMarketCap(company.getMarketCap());
        }
        if (stock.getVolume() == null) {
            stock.setVolume("0");
        }

        stockRepo.save(stock);
    }
}
