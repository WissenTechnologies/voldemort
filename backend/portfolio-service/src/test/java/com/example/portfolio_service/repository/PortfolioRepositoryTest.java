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

import com.example.portfolio_service.entites.Portfolio;

@DataJpaTest
@ActiveProfiles("test")
class PortfolioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private Portfolio portfolio1;
    private Portfolio portfolio2;
    private Portfolio portfolio3;

    @BeforeEach
    void setUp() {
        portfolio1 = new Portfolio();
        portfolio1.setUserId(1L);
        portfolio1.setName("Portfolio 1");

        portfolio2 = new Portfolio();
        portfolio2.setUserId(1L);
        portfolio2.setName("Portfolio 2");

        portfolio3 = new Portfolio();
        portfolio3.setUserId(2L);
        portfolio3.setName("Portfolio 3");
    }

    @Test
    void savePortfolio_shouldPersistPortfolio() {
        Portfolio savedPortfolio = portfolioRepository.save(portfolio1);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(savedPortfolio.getId(), retrievedPortfolio.getId());
        assertEquals(1L, retrievedPortfolio.getUserId());
        assertEquals("Portfolio 1", retrievedPortfolio.getName());
    }

    @Test
    void findById_whenPortfolioExists_shouldReturnPortfolio() {
        entityManager.persistAndFlush(portfolio1);
        
        Optional<Portfolio> foundPortfolio = portfolioRepository.findById(portfolio1.getId());
        
        assertTrue(foundPortfolio.isPresent());
        assertEquals(portfolio1.getId(), foundPortfolio.get().getId());
        assertEquals(1L, foundPortfolio.get().getUserId());
        assertEquals("Portfolio 1", foundPortfolio.get().getName());
    }

    @Test
    void findById_whenPortfolioDoesNotExist_shouldReturnEmpty() {
        Optional<Portfolio> foundPortfolio = portfolioRepository.findById(999L);
        
        assertFalse(foundPortfolio.isPresent());
    }

    @Test
    void findAll_whenPortfoliosExist_shouldReturnAllPortfolios() {
        entityManager.persistAndFlush(portfolio1);
        entityManager.persistAndFlush(portfolio2);
        entityManager.persistAndFlush(portfolio3);
        
        List<Portfolio> portfolios = portfolioRepository.findAll();
        
        assertEquals(3, portfolios.size());
        assertTrue(portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 1")));
        assertTrue(portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 2")));
        assertTrue(portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 3")));
    }

    @Test
    void findAll_whenNoPortfoliosExist_shouldReturnEmptyList() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        
        assertTrue(portfolios.isEmpty());
    }

    @Test
    void findByUserId_whenUserHasPortfolios_shouldReturnUserPortfolios() {
        entityManager.persistAndFlush(portfolio1);
        entityManager.persistAndFlush(portfolio2);
        entityManager.persistAndFlush(portfolio3);
        
        List<Portfolio> user1Portfolios = portfolioRepository.findByUserId(1L);
        
        assertEquals(2, user1Portfolios.size());
        assertTrue(user1Portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 1")));
        assertTrue(user1Portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 2")));
        assertFalse(user1Portfolios.stream().anyMatch(p -> p.getName().equals("Portfolio 3")));
    }

    @Test
    void findByUserId_whenUserHasNoPortfolios_shouldReturnEmptyList() {
        entityManager.persistAndFlush(portfolio1);
        entityManager.persistAndFlush(portfolio2);
        
        List<Portfolio> user3Portfolios = portfolioRepository.findByUserId(3L);
        
        assertTrue(user3Portfolios.isEmpty());
    }

    @Test
    void findByUserId_whenUserHasOnePortfolio_shouldReturnSinglePortfolio() {
        entityManager.persistAndFlush(portfolio3);
        
        List<Portfolio> user2Portfolios = portfolioRepository.findByUserId(2L);
        
        assertEquals(1, user2Portfolios.size());
        assertEquals("Portfolio 3", user2Portfolios.get(0).getName());
        assertEquals(2L, user2Portfolios.get(0).getUserId());
    }

    @Test
    void deleteById_shouldRemovePortfolio() {
        entityManager.persistAndFlush(portfolio1);
        
        assertTrue(portfolioRepository.findById(portfolio1.getId()).isPresent());
        
        portfolioRepository.deleteById(portfolio1.getId());
        
        assertFalse(portfolioRepository.findById(portfolio1.getId()).isPresent());
    }

    @Test
    void updatePortfolio_shouldUpdateFields() {
        entityManager.persistAndFlush(portfolio1);
        
        Portfolio portfolioToUpdate = entityManager.find(Portfolio.class, portfolio1.getId());
        portfolioToUpdate.setName("Updated Portfolio Name");
        portfolioToUpdate.setUserId(5L);
        
        Portfolio updatedPortfolio = portfolioRepository.save(portfolioToUpdate);
        
        assertEquals("Updated Portfolio Name", updatedPortfolio.getName());
        assertEquals(5L, updatedPortfolio.getUserId());
        assertEquals(portfolio1.getId(), updatedPortfolio.getId());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, portfolioRepository.count());
        
        entityManager.persistAndFlush(portfolio1);
        assertEquals(1, portfolioRepository.count());
        
        entityManager.persistAndFlush(portfolio2);
        assertEquals(2, portfolioRepository.count());
        
        entityManager.persistAndFlush(portfolio3);
        assertEquals(3, portfolioRepository.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenPortfolioExists() {
        entityManager.persistAndFlush(portfolio1);
        
        assertTrue(portfolioRepository.existsById(portfolio1.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenPortfolioDoesNotExist() {
        assertFalse(portfolioRepository.existsById(999L));
    }

    @Test
    void savePortfolio_withNullName_shouldPersistCorrectly() {
        Portfolio portfolioWithNullName = new Portfolio();
        portfolioWithNullName.setUserId(1L);
        portfolioWithNullName.setName(null);
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolioWithNullName);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(1L, retrievedPortfolio.getUserId());
        assertEquals(null, retrievedPortfolio.getName());
    }

    @Test
    void savePortfolio_withEmptyName_shouldPersistCorrectly() {
        Portfolio portfolioWithEmptyName = new Portfolio();
        portfolioWithEmptyName.setUserId(1L);
        portfolioWithEmptyName.setName("");
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolioWithEmptyName);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(1L, retrievedPortfolio.getUserId());
        assertEquals("", retrievedPortfolio.getName());
    }

    @Test
    void savePortfolio_withZeroUserId_shouldPersistCorrectly() {
        Portfolio portfolioWithZeroUserId = new Portfolio();
        portfolioWithZeroUserId.setUserId(0L);
        portfolioWithZeroUserId.setName("Zero User Portfolio");
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolioWithZeroUserId);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(0L, retrievedPortfolio.getUserId());
        assertEquals("Zero User Portfolio", retrievedPortfolio.getName());
    }

    @Test
    void savePortfolio_withNegativeUserId_shouldPersistCorrectly() {
        Portfolio portfolioWithNegativeUserId = new Portfolio();
        portfolioWithNegativeUserId.setUserId(-1L);
        portfolioWithNegativeUserId.setName("Negative User Portfolio");
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolioWithNegativeUserId);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(-1L, retrievedPortfolio.getUserId());
        assertEquals("Negative User Portfolio", retrievedPortfolio.getName());
    }

    @Test
    void savePortfolio_withMaxLongValues_shouldPersistCorrectly() {
        Portfolio portfolioWithMaxValues = new Portfolio();
        portfolioWithMaxValues.setUserId(Long.MAX_VALUE);
        portfolioWithMaxValues.setName("Max Value Portfolio with a very long name that tests the limits of the database field");
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolioWithMaxValues);

        Portfolio retrievedPortfolio = entityManager.find(Portfolio.class, savedPortfolio.getId());
        
        assertNotNull(retrievedPortfolio);
        assertEquals(Long.MAX_VALUE, retrievedPortfolio.getUserId());
        assertEquals("Max Value Portfolio with a very long name that tests the limits of the database field", retrievedPortfolio.getName());
    }

    @Test
    void findByUserId_withNonExistentUserId_shouldReturnEmptyList() {
        entityManager.persistAndFlush(portfolio1);
        entityManager.persistAndFlush(portfolio2);
        entityManager.persistAndFlush(portfolio3);
        
        List<Portfolio> nonExistentUserPortfolios = portfolioRepository.findByUserId(999L);
        
        assertTrue(nonExistentUserPortfolios.isEmpty());
    }
}
