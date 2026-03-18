package com.example.demo.Controllers;

import com.example.demo.Entities.Stock;
import com.example.demo.Entities.StockHistory;
import com.example.demo.Services.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/stocks")
@CrossOrigin("*")
@Tag(name = "Stocks", description = "Stock market data APIs")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    @Operation(summary = "Get all stocks", description = "Retrieves a list of all available stocks with current prices")
    public List<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get stock details", description = "Retrieves details for a specific stock by its symbol")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol) {
        return stockService.getStockBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{symbol}/history")
    @Operation(summary = "Get stock history", description = "Retrieves historical price data for a specific stock")
    public List<StockHistory> getStockHistory(@PathVariable String symbol) {
        return stockService.getStockHistory(symbol);
    }
}
