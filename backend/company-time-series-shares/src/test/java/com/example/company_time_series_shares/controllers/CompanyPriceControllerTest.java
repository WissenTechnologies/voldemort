package com.example.company_time_series_shares.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.company_time_series_shares.entities.CompanyPrice;
import com.example.company_time_series_shares.services.CompanyPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CompanyPriceController.class)
class CompanyPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyPriceService companyPriceService;

    @Test
    void getLatestPrice_whenValidCompanyId_returnsPrice() throws Exception {
        when(companyPriceService.getLatestPrice(1)).thenReturn(150.75);

        mockMvc.perform(get("/api/prices/latest/{companyId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(150.75));

        verify(companyPriceService).getLatestPrice(1);
    }

    @Test
    void getCandlestickData_whenValidRequest_returnsCandlestickData() throws Exception {
        List<Object> candlestickData = Arrays.asList(
                createCandlestick(LocalDateTime.of(2023, 1, 1, 10, 0), 100.0, 110.0, 95.0, 105.0),
                createCandlestick(LocalDateTime.of(2023, 1, 1, 11, 0), 105.0, 115.0, 100.0, 110.0)
        );

        when(companyPriceService.getCandlestickData(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(candlestickData);

        mockMvc.perform(get("/api/prices/candlestick/{companyId}", 1)
                        .param("start", "2023-01-01T09:00:00")
                        .param("end", "2023-01-01T12:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].time").exists())
                .andExpect(jsonPath("$[0].open").value(100.0))
                .andExpect(jsonPath("$[0].high").value(110.0))
                .andExpect(jsonPath("$[0].low").value(95.0))
                .andExpect(jsonPath("$[0].close").value(105.0));
    }

    @Test
    void getRecent_whenValidRequest_returnsRecentPrices() throws Exception {
        List<CompanyPrice> recentPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 150.0, LocalDateTime.now().minusSeconds(100)),
                createCompanyPrice(2L, 1, 155.0, LocalDateTime.now().minusSeconds(50)),
                createCompanyPrice(3L, 1, 160.0, LocalDateTime.now())
        );

        when(companyPriceService.getRecentPrices(1, 300)).thenReturn(recentPrices);

        mockMvc.perform(get("/api/prices/recent/{companyId}", 1)
                        .param("seconds", "300")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(150.0))
                .andExpect(jsonPath("$[1].value").value(155.0))
                .andExpect(jsonPath("$[2].value").value(160.0));
    }

    @Test
    void getRecent_whenDefaultSeconds_returnsRecentPrices() throws Exception {
        List<CompanyPrice> recentPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 145.0, LocalDateTime.now().minusSeconds(150))
        );

        when(companyPriceService.getRecentPrices(1, 300)).thenReturn(recentPrices);

        mockMvc.perform(get("/api/prices/recent/{companyId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(145.0));
    }

    @Test
    void getRange_whenValidRequest_returnsPricesInRange() throws Exception {
        List<CompanyPrice> rangePrices = Arrays.asList(
                createCompanyPrice(1L, 1, 100.0, LocalDateTime.of(2023, 1, 1, 10, 0)),
                createCompanyPrice(2L, 1, 110.0, LocalDateTime.of(2023, 1, 1, 11, 0)),
                createCompanyPrice(3L, 1, 120.0, LocalDateTime.of(2023, 1, 1, 12, 0))
        );

        when(companyPriceService.getPrices(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rangePrices);

        mockMvc.perform(get("/api/prices/range/{companyId}", 1)
                        .param("start", "2023-01-01T09:00:00")
                        .param("end", "2023-01-01T13:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(100.0))
                .andExpect(jsonPath("$[1].value").value(110.0))
                .andExpect(jsonPath("$[2].value").value(120.0));
    }

    @Test
    void get1Month_whenValidCompanyId_returns1MonthPrices() throws Exception {
        List<CompanyPrice> monthPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 200.0, LocalDateTime.now().minusDays(20)),
                createCompanyPrice(2L, 1, 210.0, LocalDateTime.now().minusDays(10))
        );

        when(companyPriceService.getPrices(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(monthPrices);

        mockMvc.perform(get("/api/prices/1month/{companyId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(200.0))
                .andExpect(jsonPath("$[1].value").value(210.0));
    }

    @Test
    void get6Months_whenValidCompanyId_returns6MonthsPrices() throws Exception {
        List<CompanyPrice> sixMonthsPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 180.0, LocalDateTime.now().minusMonths(3)),
                createCompanyPrice(2L, 1, 190.0, LocalDateTime.now().minusMonths(1))
        );

        when(companyPriceService.getPrices(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sixMonthsPrices);

        mockMvc.perform(get("/api/prices/6months/{companyId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(180.0))
                .andExpect(jsonPath("$[1].value").value(190.0));
    }

    @Test
    void get1Year_whenValidCompanyId_returns1YearPrices() throws Exception {
        List<CompanyPrice> yearPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 150.0, LocalDateTime.now().minusMonths(6)),
                createCompanyPrice(2L, 1, 175.0, LocalDateTime.now().minusMonths(3))
        );

        when(companyPriceService.getPrices(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(yearPrices);

        mockMvc.perform(get("/api/prices/1year/{companyId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].value").value(150.0))
                .andExpect(jsonPath("$[1].value").value(175.0));
    }

    @Test
    void getCandlestickData_whenEmptyRange_returnsEmptyList() throws Exception {
        when(companyPriceService.getCandlestickData(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/prices/candlestick/{companyId}", 1)
                        .param("start", "2023-01-01T09:00:00")
                        .param("end", "2023-01-01T10:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRecent_whenZeroSeconds_returnsRecentPrices() throws Exception {
        List<CompanyPrice> recentPrices = Arrays.asList(
                createCompanyPrice(1L, 1, 155.0, LocalDateTime.now())
        );

        when(companyPriceService.getRecentPrices(1, 0)).thenReturn(recentPrices);

        mockMvc.perform(get("/api/prices/recent/{companyId}", 1)
                        .param("seconds", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].value").value(155.0));
    }

    @Test
    void getLatestPrice_whenNoData_returnsZero() throws Exception {
        when(companyPriceService.getLatestPrice(999)).thenReturn(0.0);

        mockMvc.perform(get("/api/prices/latest/{companyId}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(0.0));
    }

    private CompanyPrice createCompanyPrice(Long id, int companyId, Double value, LocalDateTime recordedAt) {
        CompanyPrice companyPrice = new CompanyPrice();
        companyPrice.setId(id);
        companyPrice.setCompanyId(companyId);
        companyPrice.setValue(value);
        companyPrice.setRecordedAt(recordedAt);
        return companyPrice;
    }

    private Object createCandlestick(LocalDateTime time, Double open, Double high, Double low, Double close) {
        return new java.util.HashMap<String, Object>() {{
            put("time", time);
            put("open", open);
            put("high", high);
            put("low", low);
            put("close", close);
        }};
    }
}
