package com.example.portfolio_service.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.portfolio_service.client.PriceServiceClient;
import com.example.portfolio_service.dto.HoldingRequest;
import com.example.portfolio_service.dto.HoldingResponse;
import com.example.portfolio_service.dto.PortfolioResponse;
import com.example.portfolio_service.entites.Holding;
import com.example.portfolio_service.entites.Portfolio;
import com.example.portfolio_service.repository.HoldingRepository;
import com.example.portfolio_service.repository.PortfolioRepository;

import jakarta.transaction.Transactional;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepo;

    @Autowired
    private HoldingRepository holdingRepo;

    @Autowired
    private PriceServiceClient priceClient;

    // CREATE PORTFOLIO
    public Portfolio createPortfolio(Long userId, String name) {
        Portfolio p = new Portfolio();
        p.setUserId(userId);
        p.setName(name);
        return portfolioRepo.save(p);
    }

    // GET PORTFOLIO
    public PortfolioResponse getPortfolio(Long userId) {

        List<Portfolio> portfolios = portfolioRepo.findByUserId(userId);

        if (portfolios.isEmpty()) {
            return new PortfolioResponse(0.0, 0.0, 0.0, new ArrayList<>());
        }

        List<HoldingResponse> responseList = new ArrayList<>();

        double totalInvestment = 0;
        double totalCurrentValue = 0;

        for (Portfolio portfolio : portfolios) {

            List<Holding> holdings = holdingRepo.findByPortfolioId(portfolio.getId());

            for (Holding h : holdings) {

                Double currentPrice;

                try {
                    currentPrice = priceClient.getLatestPrice(h.getCompanyId());
                } catch (Exception e) {
                    currentPrice = h.getAvgPrice(); // fallback
                }

                if (currentPrice == null) currentPrice = h.getAvgPrice();

                double currentValue = h.getQuantity() * currentPrice;
                double profitLoss = currentValue - h.getTotalInvested();

                totalInvestment += h.getTotalInvested();
                totalCurrentValue += currentValue;

                responseList.add(new HoldingResponse(
                        h.getCompanyName(),
                        h.getQuantity(),
                        h.getAvgPrice(),
                        currentPrice,
                        profitLoss
                ));
            }
        }

        return new PortfolioResponse(
                totalInvestment,
                totalCurrentValue,
                totalCurrentValue - totalInvestment,
                responseList
        );
    }
    @Transactional
public void buyHolding(HoldingRequest request) {

    Holding holding = holdingRepo
            .findByPortfolioIdAndCompanyId(request.getPortfolioId(), request.getCompanyId())
            .orElse(null);

    if (holding == null) {
        // NEW HOLDING
        Holding newHolding = new Holding();

        newHolding.setPortfolioId(request.getPortfolioId());
        newHolding.setCompanyId(request.getCompanyId());
        newHolding.setCompanyName(request.getCompanyName());
        newHolding.setQuantity(request.getQuantity());
        newHolding.setAvgPrice(request.getPrice());
        newHolding.setTotalInvested(request.getQuantity() * request.getPrice());

        holdingRepo.save(newHolding);

    } else {
        // UPDATE EXISTING

        int oldQty = holding.getQuantity();
        double oldAvg = holding.getAvgPrice();

        int newQty = oldQty + request.getQuantity();

        double newAvg = ((oldQty * oldAvg) +
                (request.getQuantity() * request.getPrice())) / newQty;

        holding.setQuantity(newQty);
        holding.setAvgPrice(newAvg);
        holding.setTotalInvested(newQty * newAvg);

        holdingRepo.save(holding);
    }
}
@Transactional
public void sellHolding(HoldingRequest request) {

    Holding holding = holdingRepo
            .findByPortfolioIdAndCompanyId(request.getPortfolioId(), request.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Holding not found"));

    if (holding.getQuantity() < request.getQuantity()) {
        throw new RuntimeException("Not enough shares to sell");
    }

    int remainingQty = holding.getQuantity() - request.getQuantity();

    if (remainingQty == 0) {
        holdingRepo.delete(holding);
    } else {
        holding.setQuantity(remainingQty);
        holding.setTotalInvested(remainingQty * holding.getAvgPrice());
        holdingRepo.save(holding);
    }
}
}
