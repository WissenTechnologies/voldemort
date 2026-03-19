package com.example.company_time_series_shares.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.company_time_series_shares.entities.CompanyPrice;
import com.example.company_time_series_shares.repos.CompanyPriceRepository;

@ExtendWith(MockitoExtension.class)
class CompanyPriceServiceImplTest {

    @Mock
    private CompanyPriceRepository repository;

    @InjectMocks
    private CompanyPriceServiceImpl service;

    private CompanyPrice price1;
    private CompanyPrice price2;
    private CompanyPrice price3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        price1 = new CompanyPrice();
        price1.setId(1L);
        price1.setCompanyId(1);
        price1.setValue(150.0);
        price1.setRecordedAt(now.minusHours(2));

        price2 = new CompanyPrice();
        price2.setId(2L);
        price2.setCompanyId(1);
        price2.setValue(155.0);
        price2.setRecordedAt(now.minusHours(1));

        price3 = new CompanyPrice();
        price3.setId(3L);
        price3.setCompanyId(1);
        price3.setValue(160.0);
        price3.setRecordedAt(now);
    }

    @Test
    void getLatestPrice_whenPricesExist_returnsLatestPrice() {
        when(repository.findTopByCompanyIdOrderByRecordedAtDesc(1))
                .thenReturn(Optional.of(price3));

        Double result = service.getLatestPrice(1);

        assertEquals(160.0, result);
        verify(repository).findTopByCompanyIdOrderByRecordedAtDesc(1);
    }

    @Test
    void getLatestPrice_whenNoPricesExist_returnsZero() {
        when(repository.findTopByCompanyIdOrderByRecordedAtDesc(1))
                .thenReturn(Optional.empty());

        Double result = service.getLatestPrice(1);

        assertEquals(0.0, result);
        verify(repository).findTopByCompanyIdOrderByRecordedAtDesc(1);
    }

    @Test
    void getPrices_whenValidRange_returnsPricesInRange() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<CompanyPrice> expectedPrices = Arrays.asList(price1, price2, price3);

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end))
                .thenReturn(expectedPrices);

        List<CompanyPrice> result = service.getPrices(1, start, end);

        assertEquals(3, result.size());
        assertEquals(150.0, result.get(0).getValue());
        assertEquals(155.0, result.get(1).getValue());
        assertEquals(160.0, result.get(2).getValue());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
    }

    @Test
    void getPrices_whenEmptyRange_returnsEmptyList() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().minusMinutes(30);

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end))
                .thenReturn(Arrays.asList());

        List<CompanyPrice> result = service.getPrices(1, start, end);

        assertTrue(result.isEmpty());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
    }

    @Test
    void getRecentPrices_whenValidSeconds_returnsRecentPrices() {
        List<CompanyPrice> expectedPrices = Arrays.asList(price2, price3);

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(expectedPrices);

        List<CompanyPrice> result = service.getRecentPrices(1, 3600);

        assertEquals(2, result.size());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getRecentPrices_whenZeroSeconds_returnsAtLeastOneSecond() {
        List<CompanyPrice> expectedPrices = Arrays.asList(price3);

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(expectedPrices);

        List<CompanyPrice> result = service.getRecentPrices(1, 0);

        assertEquals(1, result.size());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getRecentPrices_whenNegativeSeconds_returnsAtLeastOneSecond() {
        List<CompanyPrice> expectedPrices = Arrays.asList(price3);

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(expectedPrices);

        List<CompanyPrice> result = service.getRecentPrices(1, -5);

        assertEquals(1, result.size());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getCandlestickData_whenPricesExist_returnsCandlestickData() {
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        
        CompanyPrice price1 = createCompanyPrice(1L, 1, 100.0, baseTime.withMinute(15));
        CompanyPrice price2 = createCompanyPrice(2L, 1, 110.0, baseTime.withMinute(30));
        CompanyPrice price3 = createCompanyPrice(3L, 1, 105.0, baseTime.withMinute(45));
        
        CompanyPrice price4 = createCompanyPrice(4L, 1, 120.0, baseTime.plusHours(1).withMinute(15));
        CompanyPrice price5 = createCompanyPrice(5L, 1, 115.0, baseTime.plusHours(1).withMinute(30));

        List<CompanyPrice> prices = Arrays.asList(price1, price2, price3, price4, price5);
        
        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(prices);

        List<Object> result = service.getCandlestickData(1, baseTime, baseTime.plusHours(2));

        assertEquals(2, result.size());
        
        // First candlestick (hour 10)
        Object candle1 = result.get(0);
        assertNotNull(candle1);
        
        // Second candlestick (hour 11)
        Object candle2 = result.get(1);
        assertNotNull(candle2);
        
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getCandlestickData_whenNoPrices_returnsEmptyList() {
        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(Arrays.asList());

        List<Object> result = service.getCandlestickData(1, LocalDateTime.now(), LocalDateTime.now());

        assertTrue(result.isEmpty());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getCandlestickData_whenSinglePrice_returnsSingleCandlestick() {
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        CompanyPrice singlePrice = createCompanyPrice(1L, 1, 100.0, baseTime.withMinute(30));

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any()))
                .thenReturn(Arrays.asList(singlePrice));

        List<Object> result = service.getCandlestickData(1, baseTime, baseTime.plusHours(1));

        assertEquals(1, result.size());
        
        Object candle = result.get(0);
        assertNotNull(candle);
        
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(anyInt(), any(), any());
    }

    @Test
    void getPrices_withNullValue_shouldHandleGracefully() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        
        CompanyPrice priceWithNullValue = new CompanyPrice();
        priceWithNullValue.setId(4L);
        priceWithNullValue.setCompanyId(1);
        priceWithNullValue.setValue(null);
        priceWithNullValue.setRecordedAt(start.plusMinutes(30));

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end))
                .thenReturn(Arrays.asList(priceWithNullValue));

        List<CompanyPrice> result = service.getPrices(1, start, end);

        assertEquals(1, result.size());
        assertEquals(null, result.get(0).getValue());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
    }

    @Test
    void getPrices_withDifferentCompanyIds_shouldFilterCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        
        CompanyPrice company1Price = createCompanyPrice(1L, 1, 150.0, start.plusMinutes(30));
        CompanyPrice company2Price = createCompanyPrice(2L, 2, 200.0, start.plusMinutes(60));

        when(repository.findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end))
                .thenReturn(Arrays.asList(company1Price));

        List<CompanyPrice> result = service.getPrices(1, start, end);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompanyId());
        assertEquals(150.0, result.get(0).getValue());
        verify(repository).findByCompanyIdAndRecordedAtBetweenOrderByRecordedAtAsc(1, start, end);
    }

    private CompanyPrice createCompanyPrice(Long id, int companyId, Double value, LocalDateTime recordedAt) {
        CompanyPrice companyPrice = new CompanyPrice();
        companyPrice.setId(id);
        companyPrice.setCompanyId(companyId);
        companyPrice.setValue(value);
        companyPrice.setRecordedAt(recordedAt);
        return companyPrice;
    }
}
