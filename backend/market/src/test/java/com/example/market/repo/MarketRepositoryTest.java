package com.example.market.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.market.entities.MarketShare;

@DataJpaTest
@ActiveProfiles("test")
class MarketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MarketRepository marketRepository;

    private MarketShare marketShare1;
    private MarketShare marketShare2;

    @BeforeEach
    void setUp() {
        marketShare1 = new MarketShare(1L, 1000000L);
        marketShare2 = new MarketShare(2L, 2000000L);
    }

    @Test
    void saveMarketShare_shouldPersistMarketShare() {
        MarketShare savedMarketShare = marketRepository.save(marketShare1);

        MarketShare retrievedMarketShare = entityManager.find(MarketShare.class, savedMarketShare.getCompanyId());
        
        assertEquals(savedMarketShare.getCompanyId(), retrievedMarketShare.getCompanyId());
        assertEquals(1000000L, retrievedMarketShare.getAvailableShares());
    }

    @Test
    void findById_whenMarketShareExists_shouldReturnMarketShare() {
        entityManager.persistAndFlush(marketShare1);
        
        Optional<MarketShare> foundMarketShare = marketRepository.findById(marketShare1.getCompanyId());
        
        assertTrue(foundMarketShare.isPresent());
        assertEquals(marketShare1.getCompanyId(), foundMarketShare.get().getCompanyId());
        assertEquals(1000000L, foundMarketShare.get().getAvailableShares());
    }

    @Test
    void findById_whenMarketShareDoesNotExist_shouldReturnEmpty() {
        Optional<MarketShare> foundMarketShare = marketRepository.findById(999L);
        
        assertFalse(foundMarketShare.isPresent());
    }

    @Test
    void findAll_whenMarketSharesExist_shouldReturnAllMarketShares() {
        entityManager.persistAndFlush(marketShare1);
        entityManager.persistAndFlush(marketShare2);
        
        List<MarketShare> marketShares = marketRepository.findAll();
        
        assertEquals(2, marketShares.size());
        assertTrue(marketShares.stream().anyMatch(m -> m.getCompanyId().equals(1L)));
        assertTrue(marketShares.stream().anyMatch(m -> m.getCompanyId().equals(2L)));
    }

    @Test
    void findAll_whenNoMarketSharesExist_shouldReturnEmptyList() {
        List<MarketShare> marketShares = marketRepository.findAll();
        
        assertTrue(marketShares.isEmpty());
    }

    @Test
    void deleteById_shouldRemoveMarketShare() {
        entityManager.persistAndFlush(marketShare1);
        
        assertTrue(marketRepository.findById(marketShare1.getCompanyId()).isPresent());
        
        marketRepository.deleteById(marketShare1.getCompanyId());
        
        assertFalse(marketRepository.findById(marketShare1.getCompanyId()).isPresent());
    }

    @Test
    void updateMarketShare_shouldUpdateAvailableShares() {
        entityManager.persistAndFlush(marketShare1);
        
        MarketShare marketShareToUpdate = entityManager.find(MarketShare.class, marketShare1.getCompanyId());
        marketShareToUpdate.setAvailableShares(1500000L);
        
        MarketShare updatedMarketShare = marketRepository.save(marketShareToUpdate);
        
        assertEquals(1500000L, updatedMarketShare.getAvailableShares());
        assertEquals(marketShare1.getCompanyId(), updatedMarketShare.getCompanyId());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, marketRepository.count());
        
        entityManager.persistAndFlush(marketShare1);
        assertEquals(1, marketRepository.count());
        
        entityManager.persistAndFlush(marketShare2);
        assertEquals(2, marketRepository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenMarketShareExists() {
        entityManager.persistAndFlush(marketShare1);
        
        assertTrue(marketRepository.existsById(marketShare1.getCompanyId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenMarketShareDoesNotExist() {
        assertFalse(marketRepository.existsById(999L));
    }

    @Test
    void saveMarketShare_withZeroAvailableShares_shouldPersistCorrectly() {
        MarketShare zeroSharesMarketShare = new MarketShare(3L, 0L);
        MarketShare savedMarketShare = marketRepository.save(zeroSharesMarketShare);

        MarketShare retrievedMarketShare = entityManager.find(MarketShare.class, savedMarketShare.getCompanyId());
        
        assertEquals(3L, retrievedMarketShare.getCompanyId());
        assertEquals(0L, retrievedMarketShare.getAvailableShares());
    }

    @Test
    void saveMarketShare_withMaxAvailableShares_shouldPersistCorrectly() {
        MarketShare maxSharesMarketShare = new MarketShare(4L, Long.MAX_VALUE);
        MarketShare savedMarketShare = marketRepository.save(maxSharesMarketShare);

        MarketShare retrievedMarketShare = entityManager.find(MarketShare.class, savedMarketShare.getCompanyId());
        
        assertEquals(4L, retrievedMarketShare.getCompanyId());
        assertEquals(Long.MAX_VALUE, retrievedMarketShare.getAvailableShares());
    }
}
