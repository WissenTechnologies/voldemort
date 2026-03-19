package com.example.company_time_series_shares.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.company_time_series_shares.entities.CompanyPrice;

@DataJpaTest
@ActiveProfiles("test")
class CompanyPriceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyPriceRepository repository;

    private CompanyPrice price1;
    private CompanyPrice price2;
    private CompanyPrice price3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        price1 = new CompanyPrice();
        price1.setCompanyId(1);
        price1.setValue(150.0);
        price1.setRecordedAt(now.minusHours(2));

        price2 = new CompanyPrice();
        price2.setCompanyId(1);
        price2.setValue(155.0);
        price2.setRecordedAt(now.minusHours(1));

        price3 = new CompanyPrice();
        price3.setCompanyId(2);
        price3.setValue(200.0);
        price3.setRecordedAt(now.minusMinutes(30));
    }

    @Test
    void saveCompanyPrice_shouldPersistCompanyPrice() {
        CompanyPrice savedPrice = repository.save(price1);

        CompanyPrice retrievedPrice = entityManager.find(CompanyPrice.class, savedPrice.getId());
        
        assertNotNull(retrievedPrice);
        assertEquals(savedPrice.getId(), retrievedPrice.getId());
        assertEquals(1, retrievedPrice.getCompanyId());
        assertEquals(150.0, retrievedPrice.getValue());
        assertNotNull(retrievedPrice.getRecordedAt());
    }

    @Test
    void findById_whenCompanyPriceExists_shouldReturnCompanyPrice() {
        entityManager.persistAndFlush(price1);
        
        Optional<CompanyPrice> foundPrice = repository.findById(price1.getId());
        
        assertTrue(foundPrice.isPresent());
        assertEquals(price1.getId(), foundPrice.get().getId());
        assertEquals(1, foundPrice.get().getCompanyId());
        assertEquals(150.0, foundPrice.get().getValue());
    }

    @Test
    void findById_whenCompanyPriceDoesNotExist_shouldReturnEmpty() {
        Optional<CompanyPrice> foundPrice = repository.findById(999L);
        
        assertFalse(foundPrice.isPresent());
    }

    @Test
    void findAll_whenCompanyPricesExist_shouldReturnAllCompanyPrices() {
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);
        entityManager.persistAndFlush(price3);
        
        List<CompanyPrice> prices = repository.findAll();
        
        assertEquals(3, prices.size());
        assertTrue(prices.stream().anyMatch(p -> p.getCompanyId() == 1 && p.getValue().equals(150.0)));
        assertTrue(prices.stream().anyMatch(p -> p.getCompanyId() == 1 && p.getValue().equals(155.0)));
        assertTrue(prices.stream().anyMatch(p -> p.getCompanyId() == 2 && p.getValue().equals(200.0)));
    }

    @Test
    void findAll_whenNoCompanyPricesExist_shouldReturnEmptyList() {
        List<CompanyPrice> prices = repository.findAll();
        
        assertTrue(prices.isEmpty());
    }

    @Test
    void findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc_whenValidRange_returnsSortedPrices() {
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);
        entityManager.persistAndFlush(price3);

        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        
        List<CompanyPrice> result = repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
        
        assertEquals(2, result.size());
        assertEquals(150.0, result.get(0).getValue());
        assertEquals(155.0, result.get(1).getValue());
        
        // Verify sorting by recordedAt ascending
        assertTrue(result.get(0).getRecordedAt().isBefore(result.get(1).getRecordedAt()) ||
                   result.get(0).getRecordedAt().equals(result.get(1).getRecordedAt()));
    }

    @Test
    void findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc_whenEmptyRange_returnsEmptyList() {
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);

        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        LocalDateTime end = LocalDateTime.now().minusMinutes(5);
        
        List<CompanyPrice> result = repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
        
        assertEquals(0, result.size());
    }

    @Test
    void findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc_whenDifferentCompanyId_filtersCorrectly() {
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);
        entityManager.persistAndFlush(price3);

        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        
        List<CompanyPrice> result = repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(2, start, end);
        
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getCompanyId());
        assertEquals(200.0, result.get(0).getValue());
    }

    @Test
    void findTopByCompanyIdOrderByRecordedAtDesc_whenPricesExist_returnsLatestPrice() {
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);
        entityManager.persistAndFlush(price3);

        Optional<CompanyPrice> result = repository.findTopByCompanyIdOrderByRecordedAtDesc(1);
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getCompanyId());
        assertEquals(155.0, result.get().getValue()); // Latest price for company 1
    }

    @Test
    void findTopByCompanyIdOrderByRecordedAtDesc_whenNoPricesExist_returnsEmpty() {
        Optional<CompanyPrice> result = repository.findTopByCompanyIdOrderByRecordedAtDesc(999);
        
        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_shouldRemoveCompanyPrice() {
        entityManager.persistAndFlush(price1);
        
        assertTrue(repository.findById(price1.getId()).isPresent());
        
        repository.deleteById(price1.getId());
        
        assertFalse(repository.findById(price1.getId()).isPresent());
    }

    @Test
    void updateCompanyPrice_shouldUpdateFields() {
        entityManager.persistAndFlush(price1);
        
        CompanyPrice priceToUpdate = entityManager.find(CompanyPrice.class, price1.getId());
        priceToUpdate.setValue(175.0);
        priceToUpdate.setCompanyId(3);
        
        CompanyPrice updatedPrice = repository.save(priceToUpdate);
        
        assertEquals(175.0, updatedPrice.getValue());
        assertEquals(3, updatedPrice.getCompanyId());
        assertEquals(price1.getId(), updatedPrice.getId());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, repository.count());
        
        entityManager.persistAndFlush(price1);
        assertEquals(1, repository.count());
        
        entityManager.persistAndFlush(price2);
        assertEquals(2, repository.count());
        
        entityManager.persistAndFlush(price3);
        assertEquals(3, repository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenCompanyPriceExists() {
        entityManager.persistAndFlush(price1);
        
        assertTrue(repository.existsById(price1.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenCompanyPriceDoesNotExist() {
        assertFalse(repository.existsById(999L));
    }

    @Test
    void saveCompanyPrice_withNullValue_shouldPersistCorrectly() {
        CompanyPrice priceWithNullValue = new CompanyPrice();
        priceWithNullValue.setCompanyId(1);
        priceWithNullValue.setValue(null);
        priceWithNullValue.setRecordedAt(LocalDateTime.now());
        
        CompanyPrice savedPrice = repository.save(priceWithNullValue);

        CompanyPrice retrievedPrice = entityManager.find(CompanyPrice.class, savedPrice.getId());
        
        assertNotNull(retrievedPrice);
        assertEquals(1, retrievedPrice.getCompanyId());
        assertEquals(null, retrievedPrice.getValue());
    }

    @Test
    void saveCompanyPrice_withZeroValue_shouldPersistCorrectly() {
        CompanyPrice priceWithZeroValue = new CompanyPrice();
        priceWithZeroValue.setCompanyId(1);
        priceWithZeroValue.setValue(0.0);
        priceWithZeroValue.setRecordedAt(LocalDateTime.now());
        
        CompanyPrice savedPrice = repository.save(priceWithZeroValue);

        CompanyPrice retrievedPrice = entityManager.find(CompanyPrice.class, savedPrice.getId());
        
        assertNotNull(retrievedPrice);
        assertEquals(1, retrievedPrice.getCompanyId());
        assertEquals(0.0, retrievedPrice.getValue());
    }

    @Test
    void saveCompanyPrice_withNegativeValue_shouldPersistCorrectly() {
        CompanyPrice priceWithNegativeValue = new CompanyPrice();
        priceWithNegativeValue.setCompanyId(1);
        priceWithNegativeValue.setValue(-50.0);
        priceWithNegativeValue.setRecordedAt(LocalDateTime.now());
        
        CompanyPrice savedPrice = repository.save(priceWithNegativeValue);

        CompanyPrice retrievedPrice = entityManager.find(CompanyPrice.class, savedPrice.getId());
        
        assertNotNull(retrievedPrice);
        assertEquals(1, retrievedPrice.getCompanyId());
        assertEquals(-50.0, retrievedPrice.getValue());
    }

    @Test
    void saveCompanyPrice_withMaxIntCompanyId_shouldPersistCorrectly() {
        CompanyPrice priceWithMaxCompanyId = new CompanyPrice();
        priceWithMaxCompanyId.setCompanyId(Integer.MAX_VALUE);
        priceWithMaxCompanyId.setValue(999.99);
        priceWithMaxCompanyId.setRecordedAt(LocalDateTime.now());
        
        CompanyPrice savedPrice = repository.save(priceWithMaxCompanyId);

        CompanyPrice retrievedPrice = entityManager.find(CompanyPrice.class, savedPrice.getId());
        
        assertNotNull(retrievedPrice);
        assertEquals(Integer.MAX_VALUE, retrievedPrice.getCompanyId());
        assertEquals(999.99, retrievedPrice.getValue());
    }

    @Test
    void findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc_withExactTimes_returnsCorrectResults() {
        LocalDateTime exactStart = price1.getRecordedAt();
        LocalDateTime exactEnd = price2.getRecordedAt();
        
        entityManager.persistAndFlush(price1);
        entityManager.persistAndFlush(price2);
        entityManager.persistAndFlush(price3);
        
        List<CompanyPrice> result = repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, exactStart, exactEnd);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getValue().equals(150.0)));
        assertTrue(result.stream().anyMatch(p -> p.getValue().equals(155.0)));
    }
}
