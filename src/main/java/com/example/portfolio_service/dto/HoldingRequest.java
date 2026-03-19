package com.example.portfolio_service.dto;

public class HoldingRequest {

    private Long portfolioId;
    private Long companyId;
    private String companyName;
    private Integer quantity;
    private Double price;

    public HoldingRequest() {}

    public HoldingRequest(Long portfolioId, Long companyId, String companyName,
                          Integer quantity, Double price) {
        this.portfolioId = portfolioId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
