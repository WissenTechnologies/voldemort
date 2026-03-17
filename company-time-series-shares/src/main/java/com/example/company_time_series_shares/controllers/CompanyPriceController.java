package com.example.company_time_series_shares.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.company_time_series_shares.services.CompanyPriceService;

@RestController
@RequestMapping("/api/prices")
public class CompanyPriceController {

    @Autowired
    private CompanyPriceService service;

    // ADD PRICE
    @PostMapping("/price/{companyId}")
    public ResponseEntity<String> addPrice(@PathVariable int companyId,
                                           @RequestParam Double value) {

        service.addPrice(companyId, value);

        return new ResponseEntity<>("Price added successfully", HttpStatus.CREATED);
    }

    // GET PRICES (DATE RANGE)
    @GetMapping("/price/{companyId}")
    public ResponseEntity<List<Double>> getPrices(@PathVariable int companyId,
                                                  @RequestParam String start,
                                                  @RequestParam String end) {

        List<Double> prices = service.getPrices(
                companyId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        );

        return new ResponseEntity<>(prices, HttpStatus.OK);
    }
}