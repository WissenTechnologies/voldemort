package com.example.portfolio_service.dto;

import java.util.List;

public class PortfolioResponse {

    private Double totalInvestment;
    private Double currentValue;
    private Double overallPL;
    private List<HoldingResponse> holdings;
    public PortfolioResponse() {}

    public PortfolioResponse(Double totalInvestment, Double currentValue,
                             Double overallPL, List<HoldingResponse> holdings) {
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.overallPL = overallPL;
        this.holdings = holdings;
    }
    public Double getTotalInvestment() {
        return totalInvestment;
    }
    public void setTotalInvestment(Double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }
    public Double getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }
    public Double getOverallPL() {
        return overallPL;
    }
    public void setOverallPL(Double overallPL) {
        this.overallPL = overallPL;
    }
    public List<HoldingResponse> getHoldings() {
        return holdings;
    }
    public void setHoldings(List<HoldingResponse> holdings) {
        this.holdings = holdings;
    }
    
}
