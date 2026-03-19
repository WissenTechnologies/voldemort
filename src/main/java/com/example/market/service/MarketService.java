package com.example.market.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.market.client.CompanyClient;
import com.example.market.entities.MarketShare;
import com.example.market.repo.MarketRepository;

@Service
public class MarketService {

    @Autowired
    private MarketRepository repo;

    @Autowired
    private CompanyClient companyClient;

    // 🔥 INIT FROM COMPANY
    public MarketShare initialize(Long companyId) {

        Map company = companyClient.getCompany(companyId);

        Long volume = ((Number) company.get("volume")).longValue();

        MarketShare market = new MarketShare(companyId, volume);

        return repo.save(market);
    }

    // GET
    public MarketShare get(Long companyId) {
        return repo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Market not initialized"));
    }

    // BUY (reduce shares)
    public void reduceShares(Long companyId, Long qty) {

        MarketShare m = get(companyId);

        if (m.getAvailableShares() < qty) {
            throw new RuntimeException("Not enough shares");
        }

        m.setAvailableShares(m.getAvailableShares() - qty);

        repo.save(m);
    }

    // SELL (increase shares)
    public void addShares(Long companyId, Long qty) {

        MarketShare m = get(companyId);

        m.setAvailableShares(m.getAvailableShares() + qty);

        repo.save(m);
    }
}
