package com.example.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.order.client.CompanyClient;
import com.example.order.client.MarketClient;
import com.example.order.client.PortfolioClient;
import com.example.order.client.PriceClient;
import com.example.order.client.WalletClient;
import com.example.order.dto.OrderRequest;
import com.example.order.entities.Order;
import com.example.order.repo.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private PriceClient priceClient;

    @Mock
    private MarketClient marketClient;

    @Mock
    private PortfolioClient portfolioClient;

    @Mock
    private WalletClient walletClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest validBuyRequest;
    private OrderRequest validSellRequest;
    private Map<String, Object> companyData;

    @BeforeEach
    void setUp() {
        validBuyRequest = new OrderRequest();
        validBuyRequest.setUserId(1L);
        validBuyRequest.setPortfolioId(1L);
        validBuyRequest.setCompanyId(1L);
        validBuyRequest.setQuantity(100);

        validSellRequest = new OrderRequest();
        validSellRequest.setUserId(1L);
        validSellRequest.setPortfolioId(1L);
        validSellRequest.setCompanyId(1L);
        validSellRequest.setQuantity(50);

        companyData = new HashMap<>();
        companyData.put("id", 1L);
        companyData.put("companyName", "Test Company");
    }

    @Test
    void buy_whenAllValidationsPass_andAllClientsSucceed_returnsOrder() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(150.0);
        when(marketClient.getAvailableShares(1L)).thenReturn(1000L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.buy(validBuyRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getPortfolioId());
        assertEquals(1L, result.getCompanyId());
        assertEquals("Test Company", result.getCompanyName());
        assertEquals("BUY", result.getType());
        assertEquals(100, result.getQuantity());
        assertEquals(150.0, result.getPrice());
        assertEquals("SUCCESS", result.getStatus());

        verify(walletClient).debit(1L, 15000.0);
        verify(marketClient).reduce(1L, 100L);
        verify(portfolioClient).buy(1L, 1L, "Test Company", 100, 150.0);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void buy_whenMissingUserId_throwsRuntimeException() {
        validBuyRequest.setUserId(null);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenMissingPortfolioId_throwsRuntimeException() {
        validBuyRequest.setPortfolioId(null);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenMissingCompanyId_throwsRuntimeException() {
        validBuyRequest.setCompanyId(null);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenInvalidQuantity_throwsRuntimeException() {
        validBuyRequest.setQuantity(0);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenPriceUnavailable_throwsRuntimeException() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenNotEnoughMarketShares_throwsRuntimeException() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(150.0);
        when(marketClient.getAvailableShares(1L)).thenReturn(50L);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenWalletFails_performsRollback() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(150.0);
        when(marketClient.getAvailableShares(1L)).thenReturn(1000L);
        doThrow(new RuntimeException("Wallet failed")).when(walletClient).debit(anyLong(), anyDouble());

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient).debit(1L, 15000.0);
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void buy_whenMarketFails_performsRollback() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(150.0);
        when(marketClient.getAvailableShares(1L)).thenReturn(1000L);
        doThrow(new RuntimeException("Market failed")).when(marketClient).reduce(anyLong(), anyLong());

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient).debit(1L, 15000.0);
        verify(marketClient).reduce(1L, 100L);
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
        verify(walletClient).credit(1L, 15000.0);
    }

    @Test
    void buy_whenPortfolioFails_performsRollback() {
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        when(priceClient.getPrice(1L)).thenReturn(150.0);
        when(marketClient.getAvailableShares(1L)).thenReturn(1000L);
        doThrow(new RuntimeException("Portfolio failed")).when(portfolioClient).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient).debit(1L, 15000.0);
        verify(marketClient).reduce(1L, 100L);
        verify(portfolioClient).buy(1L, 1L, "Test Company", 100, 150.0);
        verify(orderRepository, never()).save(any(Order.class));
        verify(walletClient).credit(1L, 15000.0);
        verify(marketClient).add(1L, 100L);
    }

    @Test
    void sell_whenAllValidationsPass_andAllClientsSucceed_returnsOrder() {
        when(priceClient.getPrice(1L)).thenReturn(200.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.sell(validSellRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getPortfolioId());
        assertEquals(1L, result.getCompanyId());
        assertEquals("", result.getCompanyName());
        assertEquals("SELL", result.getType());
        assertEquals(50, result.getQuantity());
        assertEquals(200.0, result.getPrice());
        assertEquals("SUCCESS", result.getStatus());

        verify(portfolioClient).sell(1L, 1L, 50, 200.0);
        verify(marketClient).add(1L, 50L);
        verify(walletClient).credit(1L, 10000.0);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void sell_whenMissingUserId_throwsRuntimeException() {
        validSellRequest.setUserId(null);

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient, never()).sell(anyLong(), anyLong(), anyInt(), anyDouble());
        verify(marketClient, never()).add(anyLong(), anyLong());
        verify(walletClient, never()).credit(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void sell_whenPriceUnavailable_throwsRuntimeException() {
        when(priceClient.getPrice(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient, never()).sell(anyLong(), anyLong(), anyInt(), anyDouble());
        verify(marketClient, never()).add(anyLong(), anyLong());
        verify(walletClient, never()).credit(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void sell_whenPortfolioFails_performsRollback() {
        when(priceClient.getPrice(1L)).thenReturn(200.0);
        doThrow(new RuntimeException("Portfolio failed")).when(portfolioClient).sell(anyLong(), anyLong(), anyInt(), anyDouble());

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient).sell(1L, 1L, 50, 200.0);
        verify(marketClient, never()).add(anyLong(), anyLong());
        verify(walletClient, never()).credit(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void sell_whenMarketFails_performsRollback() {
        when(priceClient.getPrice(1L)).thenReturn(200.0);
        doThrow(new RuntimeException("Market failed")).when(marketClient).add(anyLong(), anyLong());

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient).sell(1L, 1L, 50, 200.0);
        verify(marketClient).add(1L, 50L);
        verify(walletClient, never()).credit(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
        verify(portfolioClient).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
    }

    @Test
    void sell_whenWalletFails_performsRollback() {
        when(priceClient.getPrice(1L)).thenReturn(200.0);
        when(companyClient.getCompany(1L)).thenReturn(companyData);
        doThrow(new RuntimeException("Wallet failed")).when(walletClient).credit(anyLong(), anyDouble());

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient).sell(1L, 1L, 50, 200.0);
        verify(marketClient).add(1L, 50L);
        verify(walletClient).credit(1L, 10000.0);
        verify(orderRepository, never()).save(any(Order.class));
        verify(walletClient).debit(1L, 10000.0);
        verify(marketClient).reduce(1L, 50L);
        verify(portfolioClient).buy(1L, 1L, "Test Company", 50, 200.0);
    }

    @Test
    void buy_whenNegativeQuantity_throwsRuntimeException() {
        validBuyRequest.setQuantity(-10);

        assertThrows(RuntimeException.class, () -> orderService.buy(validBuyRequest));

        verify(walletClient, never()).debit(anyLong(), anyDouble());
        verify(marketClient, never()).reduce(anyLong(), anyLong());
        verify(portfolioClient, never()).buy(anyLong(), anyLong(), anyString(), anyInt(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void sell_whenNegativeQuantity_throwsRuntimeException() {
        validSellRequest.setQuantity(-5);

        assertThrows(RuntimeException.class, () -> orderService.sell(validSellRequest));

        verify(portfolioClient, never()).sell(anyLong(), anyLong(), anyInt(), anyDouble());
        verify(marketClient, never()).add(anyLong(), anyLong());
        verify(walletClient, never()).credit(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any(Order.class));
    }
}