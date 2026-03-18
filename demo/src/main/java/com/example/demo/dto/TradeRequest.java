package com.example.demo.dto;

public class TradeRequest {
    private String symbol;
    private Integer quantity;
    private String type; // BUY or SELL

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
