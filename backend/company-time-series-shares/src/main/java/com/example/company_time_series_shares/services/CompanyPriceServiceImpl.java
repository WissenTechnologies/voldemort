package com.example.company_time_series_shares.services;

import java.time.LocalDateTime;
import java.util.*;
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

    // ✅ 1. LATEST PRICE
    @Override
    public Double getLatestPrice(int companyId) {
        return repository
                .findTopByCompanyIdOrderByRecordedAtDesc(companyId)
                .map(CompanyPrice::getValue)
                .orElse(0.0);
    }

    // ✅ 2. RAW DATA
    @Override
    public List<CompanyPrice> getPrices(int companyId, LocalDateTime start, LocalDateTime end) {

        return repository
                .findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(companyId, start, end);
    }

    // ✅ 3. CANDLESTICK DATA (HOURLY GROUPING)
    @Override
    public List<Object> getCandlestickData(int companyId, LocalDateTime start, LocalDateTime end) {

        List<CompanyPrice> prices =
                repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(companyId, start, end);

        Map<LocalDateTime, List<CompanyPrice>> grouped = prices.stream()
                .collect(Collectors.groupingBy(p ->
                        p.getRecordedAt().withMinute(0).withSecond(0).withNano(0)
                ));

        List<Object> result = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<CompanyPrice>> entry : grouped.entrySet()) {

            List<CompanyPrice> list = entry.getValue();

            double open = list.get(0).getValue();
            double close = list.get(list.size() - 1).getValue();

            double high = list.stream().mapToDouble(CompanyPrice::getValue).max().orElse(0);
            double low = list.stream().mapToDouble(CompanyPrice::getValue).min().orElse(0);

            Map<String, Object> candle = new HashMap<>();
            candle.put("time", entry.getKey());
            candle.put("open", open);
            candle.put("high", high);
            candle.put("low", low);
            candle.put("close", close);

            result.add(candle);
        }

        return result;
    }
}