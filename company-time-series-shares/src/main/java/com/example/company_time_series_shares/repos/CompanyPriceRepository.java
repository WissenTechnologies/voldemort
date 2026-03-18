package com.example.company_time_series_shares.repos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.company_time_series_shares.entities.CompanyPrice;

public interface CompanyPriceRepository extends JpaRepository<CompanyPrice, Long> {

    // ✅ Sorted data (IMPORTANT for charts)
    List<CompanyPrice> findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            int companyId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ✅ Latest price
    Optional<CompanyPrice> findTopByCompanyIdOrderByRecordedAtDesc(int companyId);
}