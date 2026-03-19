package com.example.company.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.company.Entities.Company;
import com.example.company.Repos.CompanyRepo;
import com.example.company.client.MarketClient;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImpTest {

    @Mock
    private CompanyRepo repository;

    @Mock
    private MarketClient marketClient;

    @InjectMocks
    private CompanyServiceImp service;

    private Company existing;

    @BeforeEach
    void setUp() {
        existing = new Company();
        existing.setId(1);
        existing.setCompanyName("OldCo");
        existing.setSymbol("OLD");
        existing.setSector("Tech");
        existing.setIndustry("Software");
        existing.setCeo("Old CEO");
        existing.setFoundedYear(2000);
        existing.setHeadquarters("Old HQ");
        existing.setMarketCap(100.0);
        existing.setPeRatio(10.0);
        existing.setEps(1.0);
        existing.setDescription("Old Desc");
        existing.setVolume(500L);
        existing.setValue(99.0);
    }

    @Test
    void createCompany_whenMarketInitSucceeds_returnsSavedCompany() {
        Company input = new Company();
        input.setId(1);
        input.setCompanyName("NewCo");

        when(repository.save(any(Company.class))).thenReturn(input);

        Company result = service.createCompany(input);

        assertNotNull(result);
        assertEquals("NewCo", result.getCompanyName());
        verify(repository).save(input);
        verify(marketClient).initMarket(1L);
    }

    @Test
    void createCompany_whenMarketInitFails_stillReturnsSavedCompany() {
        Company input = new Company();
        input.setId(2);
        input.setCompanyName("NewCo");

        when(repository.save(any(Company.class))).thenReturn(input);
        doThrow(new RuntimeException("market down")).when(marketClient).initMarket(2L);

        Company result = service.createCompany(input);

        assertNotNull(result);
        assertEquals(2, result.getId());
        verify(repository).save(input);
        verify(marketClient).initMarket(2L);
    }

    @Test
    void getAllCompanies_returnsRepositoryList() {
        when(repository.findAll()).thenReturn(List.of(existing));

        List<Company> result = service.getAllCompanies();

        assertEquals(1, result.size());
        assertEquals("OldCo", result.get(0).getCompanyName());
        verify(repository).findAll();
    }

    @Test
    void getCompanyById_whenFound_returnsCompany() {
        when(repository.findById(1)).thenReturn(Optional.of(existing));

        Company result = service.getCompanyById(1);

        assertNotNull(result);
        assertEquals("OldCo", result.getCompanyName());
        verify(repository).findById(1);
    }

    @Test
    void getCompanyById_whenMissing_throwsRuntimeException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getCompanyById(99));
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void updateCompany_whenFound_updatesEditableFieldsOnly() {
        Company updated = new Company();
        updated.setCompanyName("NewCo");
        updated.setSymbol("NEW");
        updated.setSector("Finance");
        updated.setIndustry("Banking");
        updated.setCeo("New CEO");
        updated.setFoundedYear(2020);
        updated.setHeadquarters("New HQ");
        updated.setMarketCap(500.0);
        updated.setPeRatio(25.0);
        updated.setEps(5.0);
        updated.setDescription("New Desc");
        updated.setVolume(999L);
        updated.setValue(123.0);

        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Company result = service.updateCompany(1, updated);

        assertNotNull(result);
        assertEquals("NewCo", result.getCompanyName());
        assertEquals("NEW", result.getSymbol());
        assertEquals("Finance", result.getSector());
        assertEquals("Banking", result.getIndustry());
        assertEquals("New CEO", result.getCeo());
        assertEquals(2020, result.getFoundedYear());
        assertEquals("New HQ", result.getHeadquarters());
        assertEquals(500.0, result.getMarketCap());
        assertEquals(25.0, result.getPeRatio());
        assertEquals(5.0, result.getEps());
        assertEquals("New Desc", result.getDescription());

        // untouched by update
        assertEquals(500L, result.getVolume());
        assertEquals(99.0, result.getValue());

        verify(repository).findById(1);
        verify(repository).save(any(Company.class));
    }

    @Test
    void updateCompany_whenMissing_throwsRuntimeException_andDoesNotSave() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updateCompany(1, new Company()));

        verify(repository).findById(1);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteCompany_deletesById() {
        service.deleteCompany(5);
        verify(repository).deleteById(eq(5));
    }
}

