package com.example.order.dto;

public class OrderRequest {

    private Long userId;
    private Long portfolioId;
    private Long companyId;
    private Integer quantity;
    private Double targetPrice;
    private String orderMode; // MARKET / LIMIT / STOP_LOSS
    public OrderRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public String getOrderMode() { return orderMode; }
    public void setOrderMode(String orderMode) { this.orderMode = orderMode; }
}