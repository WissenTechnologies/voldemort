package com.example.market.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.market.entities.MarketShare;
import com.example.market.service.MarketService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MarketController.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MarketService marketService;

    @Test
    void initializeMarket_whenValidCompanyId_returnsMarketShare() throws Exception {
        MarketShare marketShare = new MarketShare(1L, 1000000L);
        when(marketService.initialize(anyLong())).thenReturn(marketShare);

        mockMvc.perform(post("/api/market/init/{companyId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.availableShares").value(1000000));

        verify(marketService).initialize(1L);
    }

    @Test
    void getMarketShare_whenCompanyIdExists_returnsMarketShare() throws Exception {
        MarketShare marketShare = new MarketShare(1L, 500000L);
        when(marketService.get(anyLong())).thenReturn(marketShare);

        mockMvc.perform(get("/api/market/{companyId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.availableShares").value(500000));

        verify(marketService).get(1L);
    }

    @Test
    void buyShares_whenSufficientShares_returnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/market/buy")
                        .param("companyId", "1")
                        .param("qty", "100")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("Shares deducted"));

        verify(marketService).reduceShares(1L, 100L);
    }

    @Test
    void buyShares_whenInsufficientShares_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Not enough shares"))
                .when(marketService).reduceShares(anyLong(), anyLong());

        mockMvc.perform(post("/api/market/buy")
                        .param("companyId", "1")
                        .param("qty", "1000000")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());

        verify(marketService).reduceShares(1L, 1000000L);
    }

    @Test
    void sellShares_whenValidRequest_returnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/market/sell")
                        .param("companyId", "1")
                        .param("qty", "50")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("Shares added"));

        verify(marketService).addShares(1L, 50L);
    }

    @Test
    void initializeMarket_whenZeroCompanyId_returnsMarketShare() throws Exception {
        MarketShare marketShare = new MarketShare(0L, 0L);
        when(marketService.initialize(0L)).thenReturn(marketShare);

        mockMvc.perform(post("/api/market/init/{companyId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyId").value(0))
                .andExpect(jsonPath("$.availableShares").value(0));

        verify(marketService).initialize(0L);
    }

    @Test
    void getMarketShare_whenCompanyDoesNotExist_initializesAndReturnsMarketShare() throws Exception {
        MarketShare marketShare = new MarketShare(999L, 2000000L);
        when(marketService.get(999L)).thenReturn(marketShare);

        mockMvc.perform(get("/api/market/{companyId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyId").value(999))
                .andExpect(jsonPath("$.availableShares").value(2000000));

        verify(marketService).get(999L);
    }

    @Test
    void buyShares_withZeroQuantity_returnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/market/buy")
                        .param("companyId", "1")
                        .param("qty", "0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("Shares deducted"));

        verify(marketService).reduceShares(1L, 0L);
    }

    @Test
    void sellShares_withZeroQuantity_returnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/market/sell")
                        .param("companyId", "1")
                        .param("qty", "0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("Shares added"));

        verify(marketService).addShares(1L, 0L);
    }

    @Test
    void buyShares_withNegativeQuantity_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Not enough shares"))
                .when(marketService).reduceShares(anyLong(), anyLong());

        mockMvc.perform(post("/api/market/buy")
                        .param("companyId", "1")
                        .param("qty", "-100")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());

        verify(marketService).reduceShares(1L, -100L);
    }
}
