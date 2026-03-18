package com.example.demo.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal currentPrice;

    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private String volume;
    private String marketCap;

    @JsonIgnoreProperties({"stock"})
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockHistory> history;

    public Stock() {}

    public Stock(String symbol, String name, BigDecimal currentPrice, BigDecimal dayHigh, BigDecimal dayLow, String volume, String marketCap) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.volume = volume;
        this.marketCap = marketCap;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getDayHigh() { return dayHigh; }
    public void setDayHigh(BigDecimal dayHigh) { this.dayHigh = dayHigh; }

    public BigDecimal getDayLow() { return dayLow; }
    public void setDayLow(BigDecimal dayLow) { this.dayLow = dayLow; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getMarketCap() { return marketCap; }
    public void setMarketCap(String marketCap) { this.marketCap = marketCap; }

    public List<StockHistory> getHistory() { return history; }
    public void setHistory(List<StockHistory> history) { this.history = history; }
}
