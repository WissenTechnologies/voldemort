package com.example.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.order.dto.OrderRequest;
import com.example.order.entities.Order;
import com.example.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void buyOrder_whenValidRequest_returnsOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        Order order = new Order(1L, 1L, 1L, "Test Company", "BUY", 100, 150.0, "SUCCESS");
        when(orderService.buy(any(OrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.type").value("BUY"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.price").value(150.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void buyOrder_whenMissingRequiredFields_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(null);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing required fields"));
    }

    @Test
    void buyOrder_whenInvalidQuantity_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(0);

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid quantity"));
    }

    @Test
    void buyOrder_whenNotEnoughShares_returnsConflict() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Not enough shares available")).when(orderService).buy(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Not enough shares available"));
    }

    @Test
    void buyOrder_whenInsufficientFunds_returnsConflict() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Insufficient wallet balance")).when(orderService).buy(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Insufficient wallet balance"));
    }

    @Test
    void buyOrder_whenCompanyNotFound_returnsNotFound() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(999L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Company not found")).when(orderService).buy(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Company not found"));
    }

    @Test
    void buyOrder_whenPriceUnavailable_returnsNotFound() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Price unavailable")).when(orderService).buy(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Price unavailable"));
    }

    @Test
    void buyOrder_whenGenericException_returnsInternalServerError() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Database connection failed")).when(orderService).buy(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Database connection failed"));
    }

    @Test
    void sellOrder_whenValidRequest_returnsOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(50);

        Order order = new Order(1L, 1L, 1L, "", "SELL", 50, 200.0, "SUCCESS");
        when(orderService.sell(any(OrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.type").value("SELL"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void sellOrder_whenMissingRequiredFields_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(null);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(50);

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing required fields"));
    }

    @Test
    void sellOrder_whenInvalidQuantity_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(-10);

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid quantity"));
    }

    @Test
    void sellOrder_whenInsufficientShares_returnsConflict() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(100);

        doThrow(new RuntimeException("Insufficient shares in portfolio")).when(orderService).sell(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Insufficient shares in portfolio"));
    }

    @Test
    void sellOrder_whenPortfolioNotFound_returnsNotFound() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(999L);
        request.setCompanyId(1L);
        request.setQuantity(50);

        doThrow(new RuntimeException("Portfolio not found")).when(orderService).sell(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Portfolio not found"));
    }

    @Test
    void sellOrder_whenGenericException_returnsInternalServerError() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(50);

        doThrow(new RuntimeException("Service temporarily unavailable")).when(orderService).sell(any(OrderRequest.class));

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Service temporarily unavailable"));
    }

    @Test
    void buyOrder_whenNegativeQuantity_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(-5);

        mockMvc.perform(post("/api/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid quantity"));
    }

    @Test
    void sellOrder_whenZeroQuantity_returnsBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setPortfolioId(1L);
        request.setCompanyId(1L);
        request.setQuantity(0);

        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid quantity"));
    }
}
