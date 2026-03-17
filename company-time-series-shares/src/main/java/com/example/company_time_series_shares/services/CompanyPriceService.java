package com.example.company_time_series_shares.services;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanyPriceService {
 
    void addPrice(int companyId, Double value);
 
    List<Double> getPrices(int companyId, LocalDateTime start, LocalDateTime end);
 
}
