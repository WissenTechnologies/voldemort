package com.example.demo.Repository;

import com.example.demo.Entities.Holding;
import com.example.demo.Entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepo extends JpaRepository<Holding, Long> {
    List<Holding> findByPortfolio(Portfolio portfolio);
    Optional<Holding> findByPortfolioAndSymbol(Portfolio portfolio, String symbol);
}
