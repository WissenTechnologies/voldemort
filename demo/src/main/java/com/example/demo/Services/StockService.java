package com.example.demo.Services;

import com.example.demo.Entities.Stock;
import com.example.demo.Entities.StockHistory;
import com.example.demo.Repository.StockHistoryRepo;
import com.example.demo.Repository.StockRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepo stockRepo;

    @Autowired
    private StockHistoryRepo historyRepo;

    public List<Stock> getAllStocks() {
        return stockRepo.findAll();
    }

    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepo.findById(symbol);
    }

    public List<StockHistory> getStockHistory(String symbol) {
        return stockRepo.findById(symbol)
                .map(stock -> historyRepo.findByStockOrderByDateAsc(stock))
                .orElse(List.of());
    }

    public Stock saveStock(Stock stock) {
        return stockRepo.save(stock);
    }

    public void saveHistory(List<StockHistory> history) {
        historyRepo.saveAll(history);
    }
}
