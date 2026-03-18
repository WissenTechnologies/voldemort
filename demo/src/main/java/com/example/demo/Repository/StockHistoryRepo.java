package com.example.demo.Repository;

import com.example.demo.Entities.Stock;
import com.example.demo.Entities.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockHistoryRepo extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByStockOrderByDateAsc(Stock stock);
}
