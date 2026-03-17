package com.example.company_time_series_shares.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.company_time_series_shares.entities.CompanyPrice;
import com.example.company_time_series_shares.repos.CompanyPriceRepository;

@Service
public class CompanyPriceServiceImpl implements CompanyPriceService {
 
    private final CompanyPriceRepository repository;
 
    public CompanyPriceServiceImpl(CompanyPriceRepository repository) {
        this.repository = repository;
    }
 
    @Override
    public void addPrice(int companyId, Double value) {
        CompanyPrice cp = new CompanyPrice();
        cp.setCompanyId(companyId);
        cp.setValue(value);
        cp.setRecordedAt(LocalDateTime.now());
        repository.save(cp);
    }
 
    @Override
    public List<Double> getPrices(int companyId, LocalDateTime start, LocalDateTime end) {
        return repository
                .findByCompanyIdAndRecordedAtBetween(companyId, start, end)
                .stream()
                .map(CompanyPrice::getValue)
                .collect(Collectors.toList());
    }
}