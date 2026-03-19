package com.example.portfolio_service.dto;

public class HoldingResponse {

    private String companyName;
    private Integer quantity;
    private Double avgPrice;
    private Double currentPrice;
    private Double profitLoss;

    public HoldingResponse() {}

    public HoldingResponse(String companyName, Integer quantity, Double avgPrice,
                           Double currentPrice, Double profitLoss) {
        this.companyName = companyName;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.currentPrice = currentPrice;
        this.profitLoss = profitLoss;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public Double getAvgPrice() {
        return avgPrice;
    }
    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }
    public Double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public Double getProfitLoss() {
        return profitLoss;
    }
    public void setProfitLoss(Double profitLoss) {
        this.profitLoss = profitLoss;
    }
    
}
