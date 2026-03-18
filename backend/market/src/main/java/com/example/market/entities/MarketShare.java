package com.example.market.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MarketShare {

    @Id
    private Long companyId;

    private Long availableShares;

    public MarketShare() {}

    public MarketShare(Long companyId, Long availableShares) {
        this.companyId = companyId;
        this.availableShares = availableShares;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getAvailableShares() {
        return availableShares;
    }

    public void setAvailableShares(Long availableShares) {
        this.availableShares = availableShares;
    }
}