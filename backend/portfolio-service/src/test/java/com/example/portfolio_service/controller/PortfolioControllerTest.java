package com.example.portfolio_service.controller;

import static org.mockito.ArgumentMatchers.any;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.portfolio_service.dto.CommonResponse;
import com.example.portfolio_service.dto.HoldingRequest;
import com.example.portfolio_service.dto.HoldingResponse;
import com.example.portfolio_service.dto.PortfolioResponse;
import com.example.portfolio_service.entites.Portfolio;
import com.example.portfolio_service.services.PortfolioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @Test
    void createPortfolio_whenValidRequest_returnsSuccess() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setUserId(1L);
        portfolio.setName("Test Portfolio");

        when(portfolioService.createPortfolio(anyLong(), any())).thenReturn(portfolio);

        mockMvc.perform(post("/api/portfolio")
                        .param("userId", "1")
                        .param("name", "Test Portfolio")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Portfolio"));
    }

    @Test
    void createPortfolio_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio")
                        .param("userId", "0")
                        .param("name", "Test Portfolio")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid userId"));
    }

    @Test
    void createPortfolio_whenNullUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio")
                        .param("name", "Test Portfolio")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid userId"));
    }

    @Test
    void createPortfolio_whenEmptyName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio")
                        .param("userId", "1")
                        .param("name", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Portfolio name is required"));
    }

    @Test
    void createPortfolio_whenNullName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Portfolio name is required"));
    }

    @Test
    void createPortfolio_whenServiceThrows_returnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(portfolioService).createPortfolio(anyLong(), any());

        mockMvc.perform(post("/api/portfolio")
                        .param("userId", "1")
                        .param("name", "Test Portfolio")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create portfolio"));
    }

    @Test
    void getPortfolio_whenValidUserId_returnsPortfolio() throws Exception {
        HoldingResponse holding = new HoldingResponse(1L, "Test Company", 100, 150.0, 200.0, 5000.0);
        PortfolioResponse portfolioResponse = new PortfolioResponse(15000.0, 20000.0, 5000.0, Arrays.asList(holding));

        when(portfolioService.getPortfolio(anyLong())).thenReturn(portfolioResponse);

        mockMvc.perform(get("/api/portfolio/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio fetched successfully"))
                .andExpect(jsonPath("$.data.totalInvestment").value(15000.0))
                .andExpect(jsonPath("$.data.totalCurrentValue").value(20000.0))
                .andExpect(jsonPath("$.data.totalProfitLoss").value(5000.0))
                .andExpect(jsonPath("$.data.holdings[0].companyId").value(1))
                .andExpect(jsonPath("$.data.holdings[0].companyName").value("Test Company"))
                .andExpect(jsonPath("$.data.holdings[0].quantity").value(100));
    }

    @Test
    void getPortfolio_whenNoHoldings_returnsNoHoldingsMessage() throws Exception {
        PortfolioResponse portfolioResponse = new PortfolioResponse(0.0, 0.0, 0.0, Collections.emptyList());

        when(portfolioService.getPortfolio(anyLong())).thenReturn(portfolioResponse);

        mockMvc.perform(get("/api/portfolio/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No holdings found"))
                .andExpect(jsonPath("$.data.holdings").isEmpty());
    }

    @Test
    void getPortfolio_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/portfolio/{userId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid userId"));
    }

    @Test
    void getPortfolio_whenServiceThrows_returnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Service error")).when(portfolioService).getPortfolio(anyLong());

        mockMvc.perform(get("/api/portfolio/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error fetching portfolio"));
    }

    @Test
    void getPortfoliosByUserId_whenValidUserId_returnsPortfolios() throws Exception {
        Portfolio portfolio1 = new Portfolio();
        portfolio1.setId(1L);
        portfolio1.setUserId(1L);
        portfolio1.setName("Portfolio 1");

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setId(2L);
        portfolio2.setUserId(1L);
        portfolio2.setName("Portfolio 2");

        List<Portfolio> portfolios = Arrays.asList(portfolio1, portfolio2);
        when(portfolioService.getPortfoliosByUserId(anyLong())).thenReturn(portfolios);

        mockMvc.perform(get("/api/portfolio/user/{userId}/portfolios", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolios fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Portfolio 1"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Portfolio 2"));
    }

    @Test
    void getPortfoliosByUserId_whenInvalidUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/portfolio/user/{userId}/portfolios", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid userId"));
    }

    @Test
    void getPortfolioByPortfolioId_whenValidId_returnsPortfolio() throws Exception {
        HoldingResponse holding = new HoldingResponse(1L, "Test Company", 50, 100.0, 120.0, 1000.0);
        PortfolioResponse portfolioResponse = new PortfolioResponse(5000.0, 6000.0, 1000.0, Arrays.asList(holding));

        when(portfolioService.getPortfolioByPortfolioId(anyLong())).thenReturn(portfolioResponse);

        mockMvc.perform(get("/api/portfolio/portfolio/{portfolioId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio fetched successfully"))
                .andExpect(jsonPath("$.data.totalInvestment").value(5000.0))
                .andExpect(jsonPath("$.data.totalCurrentValue").value(6000.0))
                .andExpect(jsonPath("$.data.totalProfitLoss").value(1000.0));
    }

    @Test
    void getPortfolioByPortfolioId_whenInvalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/portfolio/portfolio/{portfolioId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid portfolioId"));
    }

    @Test
    void buyHolding_whenValidRequest_returnsSuccess() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", 100, 150.0);

        mockMvc.perform(post("/api/portfolio/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock bought successfully"));

        verify(portfolioService).buyHolding(request);
    }

    @Test
    void buyHolding_whenInvalidQuantity_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", 0, 150.0);

        mockMvc.perform(post("/api/portfolio/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid quantity"));
    }

    @Test
    void buyHolding_whenNullQuantity_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", null, 150.0);

        mockMvc.perform(post("/api/portfolio/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid quantity"));
    }

    @Test
    void buyHolding_whenNegativeQuantity_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", -10, 150.0);

        mockMvc.perform(post("/api/portfolio/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid quantity"));
    }

    @Test
    void sellHolding_whenValidRequest_returnsSuccess() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", 50, 200.0);

        mockMvc.perform(post("/api/portfolio/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock sold successfully"));

        verify(portfolioService).sellHolding(request);
    }

    @Test
    void sellHolding_whenInvalidQuantity_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", 0, 200.0);

        mockMvc.perform(post("/api/portfolio/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid quantity"));
    }

    @Test
    void sellHolding_whenServiceThrows_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", 50, 200.0);
        doThrow(new RuntimeException("Not enough shares to sell")).when(portfolioService).sellHolding(any());

        mockMvc.perform(post("/api/portfolio/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not enough shares to sell"));
    }

    @Test
    void sellHolding_whenNullQuantity_returnsBadRequest() throws Exception {
        HoldingRequest request = new HoldingRequest(1L, 1L, "Test Company", null, 200.0);

        mockMvc.perform(post("/api/portfolio/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid quantity"));
    }
}