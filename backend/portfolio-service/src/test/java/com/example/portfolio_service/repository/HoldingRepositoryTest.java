package com.example.portfolio_service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.portfolio_service.entites.Holding;

@DataJpaTest
@ActiveProfiles("test")
class HoldingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HoldingRepository holdingRepository;

    private Holding holding1;
    private Holding holding2;
    private Holding holding3;

    @BeforeEach
    void setUp() {
        holding1 = new Holding();
        holding1.setPortfolioId(1L);
        holding1.setCompanyId(1L);
        holding1.setCompanyName("Apple Inc.");
        holding1.setQuantity(100);
        holding1.setAvgPrice(150.0);
        holding1.setTotalInvested(15000.0);

        holding2 = new Holding();
        holding2.setPortfolioId(1L);
        holding2.setCompanyId(2L);
        holding2.setCompanyName("Microsoft Corp.");
        holding2.setQuantity(50);
        holding2.setAvgPrice(200.0);
        holding2.setTotalInvested(10000.0);

        holding3 = new Holding();
        holding3.setPortfolioId(2L);
        holding3.setCompanyId(1L);
        holding3.setCompanyName("Apple Inc.");
        holding3.setQuantity(75);
        holding3.setAvgPrice(160.0);
        holding3.setTotalInvested(12000.0);
    }

    @Test
    void saveHolding_shouldPersistHolding() {
        Holding savedHolding = holdingRepository.save(holding1);

        Holding retrievedHolding = entityManager.find(Holding.class, savedHolding.getId());
        
        assertNotNull(retrievedHolding);
        assertEquals(savedHolding.getId(), retrievedHolding.getId());
        assertEquals(1L, retrievedHolding.getPortfolioId());
        assertEquals(1L, retrievedHolding.getCompanyId());
        assertEquals("Apple Inc.", retrievedHolding.getCompanyName());
        assertEquals(100, retrievedHolding.getQuantity());
        assertEquals(150.0, retrievedHolding.getAvgPrice());
        assertEquals(15000.0, retrievedHolding.getTotalInvested());
    }

    @Test
    void findById_whenHoldingExists_shouldReturnHolding() {
        entityManager.persistAndFlush(holding1);
        
        Optional<Holding> foundHolding = holdingRepository.findById(holding1.getId());
        
        assertTrue(foundHolding.isPresent());
        assertEquals(holding1.getId(), foundHolding.get().getId());
        assertEquals(1L, foundHolding.get().getPortfolioId());
        assertEquals(1L, foundHolding.get().getCompanyId());
        assertEquals("Apple Inc.", foundHolding.get().getCompanyName());
        assertEquals(100, foundHolding.get().getQuantity());
        assertEquals(150.0, foundHolding.get().getAvgPrice());
        assertEquals(15000.0, foundHolding.get().getTotalInvested());
    }

    @Test
    void findById_whenHoldingDoesNotExist_shouldReturnEmpty() {
        Optional<Holding> foundHolding = holdingRepository.findById(999L);
        
        assertFalse(foundHolding.isPresent());
    }

    @Test
    void findAll_whenHoldingsExist_shouldReturnAllHoldings() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding2);
        entityManager.persistAndFlush(holding3);
        
        List<Holding> holdings = holdingRepository.findAll();
        
        assertEquals(3, holdings.size());
        assertTrue(holdings.stream().anyMatch(h -> h.getCompanyName().equals("Apple Inc.") && h.getPortfolioId().equals(1L)));
        assertTrue(holdings.stream().anyMatch(h -> h.getCompanyName().equals("Microsoft Corp.")));
        assertTrue(holdings.stream().anyMatch(h -> h.getPortfolioId().equals(2L)));
    }

    @Test
    void findAll_whenNoHoldingsExist_shouldReturnEmptyList() {
        List<Holding> holdings = holdingRepository.findAll();
        
        assertTrue(holdings.isEmpty());
    }

    @Test
    void findByPortfolioId_whenPortfolioHasHoldings_shouldReturnPortfolioHoldings() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding2);
        entityManager.persistAndFlush(holding3);
        
        List<Holding> portfolio1Holdings = holdingRepository.findByPortfolioId(1L);
        
        assertEquals(2, portfolio1Holdings.size());
        assertTrue(portfolio1Holdings.stream().anyMatch(h -> h.getCompanyName().equals("Apple Inc.") && h.getPortfolioId().equals(1L)));
        assertTrue(portfolio1Holdings.stream().anyMatch(h -> h.getCompanyName().equals("Microsoft Corp.")));
        assertFalse(portfolio1Holdings.stream().anyMatch(h -> h.getPortfolioId().equals(2L)));
    }

    @Test
    void findByPortfolioId_whenPortfolioHasNoHoldings_shouldReturnEmptyList() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding2);
        
        List<Holding> portfolio3Holdings = holdingRepository.findByPortfolioId(3L);
        
        assertTrue(portfolio3Holdings.isEmpty());
    }

    @Test
    void findByPortfolioId_whenPortfolioHasOneHolding_shouldReturnSingleHolding() {
        entityManager.persistAndFlush(holding3);
        
        List<Holding> portfolio2Holdings = holdingRepository.findByPortfolioId(2L);
        
        assertEquals(1, portfolio2Holdings.size());
        assertEquals("Apple Inc.", portfolio2Holdings.get(0).getCompanyName());
        assertEquals(2L, portfolio2Holdings.get(0).getPortfolioId());
        assertEquals(1L, portfolio2Holdings.get(0).getCompanyId());
    }

    @Test
    void findByPortfolioIdAndCompanyId_whenHoldingExists_shouldReturnHolding() {
        entityManager.persistAndFlush(holding1);
        
        Optional<Holding> foundHolding = holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L);
        
        assertTrue(foundHolding.isPresent());
        assertEquals(1L, foundHolding.get().getPortfolioId());
        assertEquals(1L, foundHolding.get().getCompanyId());
        assertEquals("Apple Inc.", foundHolding.get().getCompanyName());
        assertEquals(100, foundHolding.get().getQuantity());
    }

    @Test
    void findByPortfolioIdAndCompanyId_whenHoldingDoesNotExist_shouldReturnEmpty() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding2);
        
        Optional<Holding> foundHolding = holdingRepository.findByPortfolioIdAndCompanyId(1L, 3L);
        
        assertFalse(foundHolding.isPresent());
    }

    @Test
    void findByPortfolioIdAndCompanyId_whenDifferentPortfolioSameCompany_shouldReturnEmpty() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding3);
        
        Optional<Holding> foundHolding = holdingRepository.findByPortfolioIdAndCompanyId(2L, 2L);
        
        assertFalse(foundHolding.isPresent());
    }

    @Test
    void deleteById_shouldRemoveHolding() {
        entityManager.persistAndFlush(holding1);
        
        assertTrue(holdingRepository.findById(holding1.getId()).isPresent());
        
        holdingRepository.deleteById(holding1.getId());
        
        assertFalse(holdingRepository.findById(holding1.getId()).isPresent());
    }

    @Test
    void updateHolding_shouldUpdateFields() {
        entityManager.persistAndFlush(holding1);
        
        Holding holdingToUpdate = entityManager.find(Holding.class, holding1.getId());
        holdingToUpdate.setQuantity(200);
        holdingToUpdate.setAvgPrice(175.0);
        holdingToUpdate.setTotalInvested(35000.0);
        holdingToUpdate.setCompanyName("Updated Apple Inc.");
        
        Holding updatedHolding = holdingRepository.save(holdingToUpdate);
        
        assertEquals(200, updatedHolding.getQuantity());
        assertEquals(175.0, updatedHolding.getAvgPrice());
        assertEquals(35000.0, updatedHolding.getTotalInvested());
        assertEquals("Updated Apple Inc.", updatedHolding.getCompanyName());
        assertEquals(holding1.getId(), updatedHolding.getId());
        assertEquals(1L, updatedHolding.getPortfolioId());
        assertEquals(1L, updatedHolding.getCompanyId());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, holdingRepository.count());
        
        entityManager.persistAndFlush(holding1);
        assertEquals(1, holdingRepository.count());
        
        entityManager.persistAndFlush(holding2);
        assertEquals(2, holdingRepository.count());
        
        entityManager.persistAndFlush(holding3);
        assertEquals(3, holdingRepository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenHoldingExists() {
        entityManager.persistAndFlush(holding1);
        
        assertTrue(holdingRepository.existsById(holding1.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenHoldingDoesNotExist() {
        assertFalse(holdingRepository.existsById(999L));
    }

    @Test
    void saveHolding_withNullCompanyName_shouldPersistCorrectly() {
        Holding holdingWithNullName = new Holding();
        holdingWithNullName.setPortfolioId(1L);
        holdingWithNullName.setCompanyId(1L);
        holdingWithNullName.setCompanyName(null);
        holdingWithNullName.setQuantity(50);
        holdingWithNullName.setAvgPrice(100.0);
        holdingWithNullName.setTotalInvested(5000.0);
        
        Holding savedHolding = holdingRepository.save(holdingWithNullName);

        Holding retrievedHolding = entityManager.find(Holding.class, savedHolding.getId());
        
        assertNotNull(retrievedHolding);
        assertEquals(1L, retrievedHolding.getPortfolioId());
        assertEquals(1L, retrievedHolding.getCompanyId());
        assertEquals(null, retrievedHolding.getCompanyName());
        assertEquals(50, retrievedHolding.getQuantity());
        assertEquals(100.0, retrievedHolding.getAvgPrice());
        assertEquals(5000.0, retrievedHolding.getTotalInvested());
    }

    @Test
    void saveHolding_withZeroQuantity_shouldPersistCorrectly() {
        Holding holdingWithZeroQuantity = new Holding();
        holdingWithZeroQuantity.setPortfolioId(1L);
        holdingWithZeroQuantity.setCompanyId(1L);
        holdingWithZeroQuantity.setCompanyName("Zero Quantity Company");
        holdingWithZeroQuantity.setQuantity(0);
        holdingWithZeroQuantity.setAvgPrice(100.0);
        holdingWithZeroQuantity.setTotalInvested(0.0);
        
        Holding savedHolding = holdingRepository.save(holdingWithZeroQuantity);

        Holding retrievedHolding = entityManager.find(Holding.class, savedHolding.getId());
        
        assertNotNull(retrievedHolding);
        assertEquals(0, retrievedHolding.getQuantity());
        assertEquals(100.0, retrievedHolding.getAvgPrice());
        assertEquals(0.0, retrievedHolding.getTotalInvested());
    }

    @Test
    void saveHolding_withNegativeValues_shouldPersistCorrectly() {
        Holding holdingWithNegativeValues = new Holding();
        holdingWithNegativeValues.setPortfolioId(1L);
        holdingWithNegativeValues.setCompanyId(1L);
        holdingWithNegativeValues.setCompanyName("Negative Values Company");
        holdingWithNegativeValues.setQuantity(-10);
        holdingWithNegativeValues.setAvgPrice(-50.0);
        holdingWithNegativeValues.setTotalInvested(500.0);
        
        Holding savedHolding = holdingRepository.save(holdingWithNegativeValues);

        Holding retrievedHolding = entityManager.find(Holding.class, savedHolding.getId());
        
        assertNotNull(retrievedHolding);
        assertEquals(-10, retrievedHolding.getQuantity());
        assertEquals(-50.0, retrievedHolding.getAvgPrice());
        assertEquals(500.0, retrievedHolding.getTotalInvested());
    }

    @Test
    void saveHolding_withMaxValues_shouldPersistCorrectly() {
        Holding holdingWithMaxValues = new Holding();
        holdingWithMaxValues.setPortfolioId(Long.MAX_VALUE);
        holdingWithMaxValues.setCompanyId(Long.MAX_VALUE);
        holdingWithMaxValues.setCompanyName("Max Values Company");
        holdingWithMaxValues.setQuantity(Integer.MAX_VALUE);
        holdingWithMaxValues.setAvgPrice(Double.MAX_VALUE);
        holdingWithMaxValues.setTotalInvested(Double.MAX_VALUE);
        
        Holding savedHolding = holdingRepository.save(holdingWithMaxValues);

        Holding retrievedHolding = entityManager.find(Holding.class, savedHolding.getId());
        
        assertNotNull(retrievedHolding);
        assertEquals(Long.MAX_VALUE, retrievedHolding.getPortfolioId());
        assertEquals(Long.MAX_VALUE, retrievedHolding.getCompanyId());
        assertEquals("Max Values Company", retrievedHolding.getCompanyName());
        assertEquals(Integer.MAX_VALUE, retrievedHolding.getQuantity());
        assertEquals(Double.MAX_VALUE, retrievedHolding.getAvgPrice());
        assertEquals(Double.MAX_VALUE, retrievedHolding.getTotalInvested());
    }

    @Test
    void findByPortfolioId_withNonExistentPortfolioId_shouldReturnEmptyList() {
        entityManager.persistAndFlush(holding1);
        entityManager.persistAndFlush(holding2);
        entityManager.persistAndFlush(holding3);
        
        List<Holding> nonExistentPortfolioHoldings = holdingRepository.findByPortfolioId(999L);
        
        assertTrue(nonExistentPortfolioHoldings.isEmpty());
    }
}
