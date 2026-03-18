package com.example.demo.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String ticker;

    @Column(nullable = false)
    private String sector;

    private String website;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(nullable = false)
    private String marketCap;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Long totalShares = 0L;

    @Column(nullable = false)
    private Long outstandingShares = 0L;

    @Column(nullable = false)
    private Long availableShares = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.totalShares == null) this.totalShares = 0L;
        if (this.outstandingShares == null) this.outstandingShares = 0L;
        if (this.availableShares == null) this.availableShares = 0L;
    }

    public Company() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getMarketCap() { return marketCap; }
    public void setMarketCap(String marketCap) { this.marketCap = marketCap; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getTotalShares() { return totalShares; }
    public void setTotalShares(Long totalShares) { this.totalShares = totalShares; }

    public Long getOutstandingShares() { return outstandingShares; }
    public void setOutstandingShares(Long outstandingShares) { this.outstandingShares = outstandingShares; }

    public Long getAvailableShares() { return availableShares; }
    public void setAvailableShares(Long availableShares) { this.availableShares = availableShares; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
