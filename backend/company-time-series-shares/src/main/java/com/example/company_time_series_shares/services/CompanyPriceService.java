package com.example.company_time_series_shares.services;

import java.time.LocalDateTime;
import java.util.List;

import com.example.company_time_series_shares.entities.CompanyPrice;

public interface CompanyPriceService {

    // Latest price
    Double getLatestPrice(int companyId);

    // Raw data (for charts)
    List<CompanyPrice> getPrices(int companyId, LocalDateTime start, LocalDateTime end);

    List<CompanyPrice> getRecentPrices(int companyId, int seconds);

    // Candlestick data
    List<Object> getCandlestickData(int companyId, LocalDateTime start, LocalDateTime end);
}