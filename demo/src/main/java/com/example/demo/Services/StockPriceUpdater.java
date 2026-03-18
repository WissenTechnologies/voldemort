package com.example.demo.Services;

import com.example.demo.Entities.Stock;
import com.example.demo.Entities.StockHistory;
import com.example.demo.Repository.StockRepo;
import com.example.demo.Repository.StockHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class StockPriceUpdater {

    @Autowired
    private StockRepo stockRepo;

    @Autowired
    private StockHistoryRepo stockHistoryRepo;

    private final Random random = new Random();

    // Runs every 1 second
    @Scheduled(fixedRate = 1000)
    public void updatePrices() {
        List<Stock> stocks = stockRepo.findAll();
        for (Stock stock : stocks) {
            BigDecimal currentPrice = stock.getCurrentPrice();
            
            // Random fluctuation: max 0.5% every 1 second
            double changePercent = (random.nextDouble() - 0.5) * 0.01; 
            BigDecimal variation = currentPrice.multiply(BigDecimal.valueOf(changePercent));
            BigDecimal newPrice = currentPrice.add(variation).setScale(2, RoundingMode.HALF_UP);
            
            stock.setCurrentPrice(newPrice);
            
            // Update day high/low
            if (newPrice.compareTo(stock.getDayHigh()) > 0) stock.setDayHigh(newPrice);
            if (newPrice.compareTo(stock.getDayLow()) < 0) stock.setDayLow(newPrice);
            
            stockRepo.save(stock);
            
            // Record to history
            StockHistory history = new StockHistory(stock, LocalDateTime.now(), newPrice);
            stockHistoryRepo.save(history);
        }
        System.out.println("Real-time prices updated at " + LocalDateTime.now());
    }
}
