package com.example.market.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.market.client.CompanyClient;
import com.example.market.entities.MarketShare;
import com.example.market.repo.MarketRepository;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock
    private MarketRepository marketRepository;

    @Mock
    private CompanyClient companyClient;

    @InjectMocks
    private MarketService marketService;

    private Map<String, Object> companyData;
    private MarketShare existingMarketShare;

    @BeforeEach
    void setUp() {
        companyData = new HashMap<>();
        companyData.put("id", 1L);
        companyData.put("volume", 1000000L);

        existingMarketShare = new MarketShare(1L, 500000L);
    }

    @Test
    void initialize_whenCompanyExists_createsMarketShareWithCompanyVolume() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketShare result = marketService.initialize(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCompanyId());
        assertEquals(1000000L, result.getAvailableShares());
        verify(companyClient).getCompany(1L);
        verify(marketRepository).save(any(MarketShare.class));
    }

    @Test
    void initialize_whenCompanyVolumeIsNull_createsMarketShareWithZeroShares() {
        companyData.put("volume", null);
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketShare result = marketService.initialize(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCompanyId());
        assertEquals(0L, result.getAvailableShares());
        verify(companyClient).getCompany(1L);
        verify(marketRepository).save(any(MarketShare.class));
    }

    @Test
    void initialize_whenCompanyVolumeIsInteger_createsMarketShareWithCorrectShares() {
        companyData.put("volume", 2000000);
        when(companyClient.getCompany(2L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketShare result = marketService.initialize(2L);

        assertNotNull(result);
        assertEquals(2L, result.getCompanyId());
        assertEquals(2000000L, result.getAvailableShares());
        verify(companyClient).getCompany(2L);
        verify(marketRepository).save(any(MarketShare.class));
    }

    @Test
    void get_whenMarketShareExists_returnsExistingMarketShare() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));

        MarketShare result = marketService.get(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCompanyId());
        assertEquals(500000L, result.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(companyClient, never()).getCompany(anyLong());
        verify(marketRepository, never()).save(any(MarketShare.class));
    }

    @Test
    void get_whenMarketShareDoesNotExist_initializesAndReturnsNewMarketShare() {
        when(marketRepository.findById(999L)).thenReturn(Optional.empty());
        when(companyClient.getCompany(999L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketShare result = marketService.get(999L);

        assertNotNull(result);
        assertEquals(999L, result.getCompanyId());
        assertEquals(1000000L, result.getAvailableShares());
        verify(marketRepository).findById(999L);
        verify(companyClient).getCompany(999L);
        verify(marketRepository).save(any(MarketShare.class));
    }

    @Test
    void reduceShares_whenSufficientShares_reducesSharesAndSaves() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.reduceShares(1L, 100000L);

        assertEquals(400000L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository).save(existingMarketShare);
    }

    @Test
    void reduceShares_whenInsufficientShares_throwsRuntimeException() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));

        assertThrows(RuntimeException.class, () -> marketService.reduceShares(1L, 600000L));

        assertEquals(500000L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository, never()).save(any(MarketShare.class));
    }

    @Test
    void reduceShares_whenExactSharesAvailable_reducesToZero() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.reduceShares(1L, 500000L);

        assertEquals(0L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository).save(existingMarketShare);
    }

    @Test
    void reduceShares_withZeroQuantity_doesNotChangeShares() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.reduceShares(1L, 0L);

        assertEquals(500000L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository).save(existingMarketShare);
    }

    @Test
    void addShares_increasesSharesAndSaves() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.addShares(1L, 200000L);

        assertEquals(700000L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository).save(existingMarketShare);
    }

    @Test
    void addShares_withZeroQuantity_doesNotChangeShares() {
        when(marketRepository.findById(1L)).thenReturn(Optional.of(existingMarketShare));
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.addShares(1L, 0L);

        assertEquals(500000L, existingMarketShare.getAvailableShares());
        verify(marketRepository).findById(1L);
        verify(marketRepository).save(existingMarketShare);
    }

    @Test
    void addShares_whenMarketShareDoesNotExist_initializesAndAddsShares() {
        when(marketRepository.findById(999L)).thenReturn(Optional.empty());
        when(companyClient.getCompany(999L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.addShares(999L, 500000L);

        verify(marketRepository).findById(999L);
        verify(companyClient).getCompany(999L);
        verify(marketRepository).save(any(MarketShare.class));
    }

    @Test
    void reduceShares_whenMarketShareDoesNotExist_initializesAndReducesShares() {
        when(marketRepository.findById(999L)).thenReturn(Optional.empty());
        when(companyClient.getCompany(999L)).thenReturn(companyData);
        when(marketRepository.save(any(MarketShare.class))).thenAnswer(inv -> inv.getArgument(0));

        marketService.reduceShares(999L, 100000L);

        verify(marketRepository).findById(999L);
        verify(companyClient).getCompany(999L);
        verify(marketRepository).save(any(MarketShare.class));
    }
}
