package com.example.demo.Repository;

import com.example.demo.Entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepo extends JpaRepository<Company, Long> {
    Optional<Company> findByTickerIgnoreCase(String ticker);
}
