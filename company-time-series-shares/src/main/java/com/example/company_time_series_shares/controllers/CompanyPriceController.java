package com.example.company_time_series_shares.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.company_time_series_shares.entities.CompanyPrice;
import com.example.company_time_series_shares.services.CompanyPriceService;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*") // Allow CORS for all origins (for testing)
public class CompanyPriceController {

    @Autowired
    private CompanyPriceService service;

    // ✅ 1. GET LATEST PRICE
    @GetMapping("/latest/{companyId}")
    public ResponseEntity<Double> getLatestPrice(@PathVariable int companyId) {

        Double price = service.getLatestPrice(companyId);

        return ResponseEntity.ok(price);
    }

    // ✅ 2. GET CANDLESTICK DATA
    // (Grouped OHLC → Open, High, Low, Close)
    @GetMapping("/candlestick/{companyId}")
    public ResponseEntity<?> getCandlestickData(
            @PathVariable int companyId,
            @RequestParam String start,
            @RequestParam String end) {

        return ResponseEntity.ok(
                service.getCandlestickData(
                        companyId,
                        LocalDateTime.parse(start),
                        LocalDateTime.parse(end)
                )
        );
    }

    // ✅ 3. GET CUSTOM RANGE (RAW DATA)
    @GetMapping("/range/{companyId}")
    public ResponseEntity<List<CompanyPrice>> getRange(
            @PathVariable int companyId,
            @RequestParam String start,
            @RequestParam String end) {

        return ResponseEntity.ok(
                service.getPrices(
                        companyId,
                        LocalDateTime.parse(start),
                        LocalDateTime.parse(end)
                )
        );
    }

    // ✅ 4. LAST 1 MONTH
    @GetMapping("/1month/{companyId}")
    public ResponseEntity<List<CompanyPrice>> get1Month(@PathVariable int companyId) {

        return ResponseEntity.ok(
                service.getPrices(
                        companyId,
                        LocalDateTime.now().minus(1, ChronoUnit.MONTHS),
                        LocalDateTime.now()
                )
        );
    }

    // ✅ 5. LAST 6 MONTHS
    @GetMapping("/6months/{companyId}")
    public ResponseEntity<List<CompanyPrice>> get6Months(@PathVariable int companyId) {

        return ResponseEntity.ok(
                service.getPrices(
                        companyId,
                        LocalDateTime.now().minus(6, ChronoUnit.MONTHS),
                        LocalDateTime.now()
                )
        );
    }

    // ✅ 6. LAST 1 YEAR
    @GetMapping("/1year/{companyId}")
    public ResponseEntity<List<CompanyPrice>> get1Year(@PathVariable int companyId) {

        return ResponseEntity.ok(
                service.getPrices(
                        companyId,
                        LocalDateTime.now().minus(1, ChronoUnit.YEARS),
                        LocalDateTime.now()
                )
        );
    }
}