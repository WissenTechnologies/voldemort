package com.example.company_time_series_shares.scheduler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.company_time_series_shares.entities.CompanyPrice;
import com.example.company_time_series_shares.repos.CompanyPriceRepository;

@Component
public class PriceScheduler {

    private final CompanyPriceRepository priceRepository;
    private final RestTemplate restTemplate;

    private final Random random = new Random();
    private final ConcurrentHashMap<Integer, Object> locks = new ConcurrentHashMap<>();

    public PriceScheduler(CompanyPriceRepository priceRepository,
                          RestTemplate restTemplate) {
        this.priceRepository = priceRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void generatePrices() {

        LocalTime now = LocalTime.now();

        LocalTime marketOpen = LocalTime.of(9, 0);
        LocalTime marketClose = LocalTime.of(15, 30);

        // Market closed → skip
        if (now.isBefore(marketOpen) || now.isAfter(marketClose)) {
            return;
        }

        // Call company microservice
        String url = "http://localhost:5001/api/companies";

        List<?> companies = restTemplate.getForObject(url, List.class);

        if (companies == null) return;

        companies.parallelStream().forEach(company -> {

            Map<?, ?> comp = (Map<?, ?>) company;

            Integer companyId = (Integer) comp.get("id");

            // 👇 IMPORTANT FIX: fetch initial price
            Double initialValue = comp.get("value") != null
                    ? ((Number) comp.get("value")).doubleValue()
                    : null;

            generatePrice(companyId, initialValue);
        });
    }

    private void generatePrice(int companyId, Double initialValue) {

        Object lock = locks.computeIfAbsent(companyId, k -> new Object());

        synchronized (lock) {

            Optional<CompanyPrice> lastPrice =
                    priceRepository.findTopByCompanyIdOrderByRecordedAtDesc(companyId);

            // 👇 CORE FIX HERE
            double basePrice = lastPrice
                    .map(CompanyPrice::getValue)
                    .orElse(initialValue != null ? initialValue : 100.0);

            double newPrice = generatePriceValue(basePrice);

            CompanyPrice cp = new CompanyPrice();
            cp.setCompanyId(companyId);
            cp.setValue(newPrice);
            cp.setRecordedAt(LocalDateTime.now());

            priceRepository.save(cp);
        }
    }

    private double generatePriceValue(double lastPrice) {

        // random change between -5% to +5%
        double percentChange = (random.nextDouble() * 10) - 5;

        double newPrice = lastPrice + (lastPrice * percentChange / 100);

        return Math.round(newPrice * 100.0) / 100.0;
    }
}