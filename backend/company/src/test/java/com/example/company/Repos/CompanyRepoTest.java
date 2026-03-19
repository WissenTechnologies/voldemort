package com.example.company.Repos;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.company.Entities.Company;

@DataJpaTest
@ActiveProfiles("test")
class CompanyRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyRepo companyRepo;

    private Company company1;
    private Company company2;

    @BeforeEach
    void setUp() {
        company1 = new Company();
        company1.setId(1);
        company1.setCompanyName("Apple Inc.");
        company1.setSymbol("AAPL");
        company1.setSector("Technology");
        company1.setIndustry("Consumer Electronics");
        company1.setCeo("Tim Cook");
        company1.setFoundedYear(1976);
        company1.setHeadquarters("Cupertino, CA");
        company1.setMarketCap(3000000000000.0);
        company1.setPeRatio(25.5);
        company1.setEps(6.05);
        company1.setDescription("Technology company that designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories.");
        company1.setVolume(1000000L);
        company1.setValue(150.0);

        company2 = new Company();
        company2.setId(2);
        company2.setCompanyName("Microsoft Corporation");
        company2.setSymbol("MSFT");
        company2.setSector("Technology");
        company2.setIndustry("Software");
        company2.setCeo("Satya Nadella");
        company2.setFoundedYear(1975);
        company2.setHeadquarters("Redmond, WA");
        company2.setMarketCap(2500000000000.0);
        company2.setPeRatio(30.2);
        company2.setEps(8.12);
        company2.setDescription("Technology company that develops, manufactures, licenses, supports, and sells computer software, consumer electronics, personal computers, and related services.");
        company2.setVolume(800000L);
        company2.setValue(320.0);
    }

    @Test
    void saveCompany_shouldPersistCompany() {
        Company savedCompany = companyRepo.save(company1);

        Company retrievedCompany = entityManager.find(Company.class, savedCompany.getId());
        
        assertEquals(savedCompany.getId(), retrievedCompany.getId());
        assertEquals("Apple Inc.", retrievedCompany.getCompanyName());
        assertEquals("AAPL", retrievedCompany.getSymbol());
        assertEquals("Technology", retrievedCompany.getSector());
        assertEquals("Consumer Electronics", retrievedCompany.getIndustry());
        assertEquals("Tim Cook", retrievedCompany.getCeo());
        assertEquals(1976, retrievedCompany.getFoundedYear());
        assertEquals("Cupertino, CA", retrievedCompany.getHeadquarters());
        assertEquals(3000000000000.0, retrievedCompany.getMarketCap());
        assertEquals(25.5, retrievedCompany.getPeRatio());
        assertEquals(6.05, retrievedCompany.getEps());
        assertEquals("Technology company that designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories.", retrievedCompany.getDescription());
        assertEquals(1000000L, retrievedCompany.getVolume());
        assertEquals(150.0, retrievedCompany.getValue());
    }

    @Test
    void findById_whenCompanyExists_shouldReturnCompany() {
        entityManager.persistAndFlush(company1);
        
        Optional<Company> foundCompany = companyRepo.findById(company1.getId());
        
        assertTrue(foundCompany.isPresent());
        assertEquals(company1.getId(), foundCompany.get().getId());
        assertEquals("Apple Inc.", foundCompany.get().getCompanyName());
    }

    @Test
    void findById_whenCompanyDoesNotExist_shouldReturnEmpty() {
        Optional<Company> foundCompany = companyRepo.findById(999);
        
        assertFalse(foundCompany.isPresent());
    }

    @Test
    void findAll_whenCompaniesExist_shouldReturnAllCompanies() {
        entityManager.persistAndFlush(company1);
        entityManager.persistAndFlush(company2);
        
        List<Company> companies = companyRepo.findAll();
        
        assertEquals(2, companies.size());
        assertTrue(companies.stream().anyMatch(c -> c.getCompanyName().equals("Apple Inc.")));
        assertTrue(companies.stream().anyMatch(c -> c.getCompanyName().equals("Microsoft Corporation")));
    }

    @Test
    void findAll_whenNoCompaniesExist_shouldReturnEmptyList() {
        List<Company> companies = companyRepo.findAll();
        
        assertTrue(companies.isEmpty());
    }

    @Test
    void deleteById_shouldRemoveCompany() {
        entityManager.persistAndFlush(company1);
        
        assertTrue(companyRepo.findById(company1.getId()).isPresent());
        
        companyRepo.deleteById(company1.getId());
        
        assertFalse(companyRepo.findById(company1.getId()).isPresent());
    }

    @Test
    void updateCompany_shouldUpdateFields() {
        entityManager.persistAndFlush(company1);
        
        Company companyToUpdate = entityManager.find(Company.class, company1.getId());
        companyToUpdate.setCompanyName("Updated Apple Inc.");
        companyToUpdate.setMarketCap(3500000000000.0);
        
        Company updatedCompany = companyRepo.save(companyToUpdate);
        
        assertEquals("Updated Apple Inc.", updatedCompany.getCompanyName());
        assertEquals(3500000000000.0, updatedCompany.getMarketCap());
        assertEquals(company1.getId(), updatedCompany.getId());
        assertEquals("AAPL", updatedCompany.getSymbol());
    }

    @Test
    void count_shouldReturnCorrectCount() {
        assertEquals(0, companyRepo.count());
        
        entityManager.persistAndFlush(company1);
        assertEquals(1, companyRepo.count());
        
        entityManager.persistAndFlush(company2);
        assertEquals(2, companyRepo.count());
    }

    @Test
    void existsById_shouldReturnTrueWhenCompanyExists() {
        entityManager.persistAndFlush(company1);
        
        assertTrue(companyRepo.existsById(company1.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenCompanyDoesNotExist() {
        assertFalse(companyRepo.existsById(999));
    }
}
