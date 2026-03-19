package com.example.portfolio_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.portfolio_service.client.PriceServiceClient;
import com.example.portfolio_service.dto.HoldingRequest;
import com.example.portfolio_service.dto.HoldingResponse;
import com.example.portfolio_service.dto.PortfolioResponse;
import com.example.portfolio_service.entites.Holding;
import com.example.portfolio_service.entites.Portfolio;
import com.example.portfolio_service.repository.HoldingRepository;
import com.example.portfolio_service.repository.PortfolioRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private PriceServiceClient priceServiceClient;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio portfolio;
    private Holding holding;
    private HoldingRequest buyRequest;
    private HoldingRequest sellRequest;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setUserId(1L);
        portfolio.setName("Test Portfolio");

        holding = new Holding();
        holding.setId(1L);
        holding.setPortfolioId(1L);
        holding.setCompanyId(1L);
        holding.setCompanyName("Test Company");
        holding.setQuantity(100);
        holding.setAvgPrice(150.0);
        holding.setTotalInvested(15000.0);

        buyRequest = new HoldingRequest(1L, 1L, "Test Company", 50, 200.0);
        sellRequest = new HoldingRequest(1L, 1L, "Test Company", 30, 180.0);
    }

    @Test
    void createPortfolio_shouldSaveAndReturnPortfolio() {
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        Portfolio result = portfolioService.createPortfolio(1L, "Test Portfolio");

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("Test Portfolio", result.getName());
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void getPortfolio_whenUserHasPortfoliosAndHoldings_returnsPortfolioResponse() {
        List<Portfolio> portfolios = Arrays.asList(portfolio);
        List<Holding> holdings = Arrays.asList(holding);

        when(portfolioRepository.findByUserId(1L)).thenReturn(portfolios);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(holdings);
        when(priceServiceClient.getLatestPrice(1L)).thenReturn(200.0);

        PortfolioResponse result = portfolioService.getPortfolio(1L);

        assertNotNull(result);
        assertEquals(15000.0, result.getTotalInvestment());
        assertEquals(20000.0, result.getCurrentValue());
        assertEquals(5000.0, result.getOverallPL());
        assertEquals(1, result.getHoldings().size());

        HoldingResponse holdingResponse = result.getHoldings().get(0);
        assertEquals(1L, holdingResponse.getCompanyId());
        assertEquals("Test Company", holdingResponse.getCompanyName());
        assertEquals(100, holdingResponse.getQuantity());
        assertEquals(150.0, holdingResponse.getAvgPrice());
        assertEquals(200.0, holdingResponse.getCurrentPrice());
        assertEquals(5000.0, holdingResponse.getProfitLoss());

        verify(portfolioRepository).findByUserId(1L);
        verify(holdingRepository).findByPortfolioId(1L);
        verify(priceServiceClient).getLatestPrice(1L);
    }

    @Test
    void getPortfolio_whenPriceServiceFails_usesAvgPriceAsFallback() {
        List<Portfolio> portfolios = Arrays.asList(portfolio);
        List<Holding> holdings = Arrays.asList(holding);

        when(portfolioRepository.findByUserId(1L)).thenReturn(portfolios);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(holdings);
        doThrow(new RuntimeException("Price service unavailable")).when(priceServiceClient).getLatestPrice(1L);

        PortfolioResponse result = portfolioService.getPortfolio(1L);

        assertNotNull(result);
        assertEquals(15000.0, result.getTotalInvestment());
        assertEquals(15000.0, result.getCurrentValue());
        assertEquals(0.0, result.getOverallPL());

        HoldingResponse holdingResponse = result.getHoldings().get(0);
        assertEquals(150.0, holdingResponse.getCurrentPrice()); // Fallback to avg price
        assertEquals(0.0, holdingResponse.getProfitLoss());
    }

    @Test
    void getPortfolio_whenPriceServiceReturnsNull_usesAvgPriceAsFallback() {
        List<Portfolio> portfolios = Arrays.asList(portfolio);
        List<Holding> holdings = Arrays.asList(holding);

        when(portfolioRepository.findByUserId(1L)).thenReturn(portfolios);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(holdings);
        when(priceServiceClient.getLatestPrice(1L)).thenReturn(null);

        PortfolioResponse result = portfolioService.getPortfolio(1L);

        assertNotNull(result);
        assertEquals(15000.0, result.getTotalInvestment());
        assertEquals(15000.0, result.getCurrentValue());
        assertEquals(0.0, result.getOverallPL());

        HoldingResponse holdingResponse = result.getHoldings().get(0);
        assertEquals(150.0, holdingResponse.getCurrentPrice()); // Fallback to avg price
    }

    @Test
    void getPortfolio_whenUserHasNoPortfolios_returnsEmptyPortfolioResponse() {
        when(portfolioRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        PortfolioResponse result = portfolioService.getPortfolio(1L);

        assertNotNull(result);
        assertEquals(0.0, result.getTotalInvestment());
        assertEquals(0.0, result.getCurrentValue());
        assertEquals(0.0, result.getOverallPL());
        assertTrue(result.getHoldings().isEmpty());

        verify(portfolioRepository).findByUserId(1L);
        verify(holdingRepository, never()).findByPortfolioId(anyLong());
        verify(priceServiceClient, never()).getLatestPrice(anyLong());
    }

    @Test
    void getPortfolio_whenPortfolioHasNoHoldings_returnsEmptyPortfolioResponse() {
        List<Portfolio> portfolios = Arrays.asList(portfolio);

        when(portfolioRepository.findByUserId(1L)).thenReturn(portfolios);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(Collections.emptyList());

        PortfolioResponse result = portfolioService.getPortfolio(1L);

        assertNotNull(result);
        assertEquals(0.0, result.getTotalInvestment());
        assertEquals(0.0, result.getCurrentValue());
        assertEquals(0.0, result.getOverallPL());
        assertTrue(result.getHoldings().isEmpty());

        verify(portfolioRepository).findByUserId(1L);
        verify(holdingRepository).findByPortfolioId(1L);
    }

    @Test
    void getPortfoliosByUserId_shouldReturnPortfolios() {
        List<Portfolio> portfolios = Arrays.asList(portfolio);
        when(portfolioRepository.findByUserId(1L)).thenReturn(portfolios);

        List<Portfolio> result = portfolioService.getPortfoliosByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("Test Portfolio", result.get(0).getName());

        verify(portfolioRepository).findByUserId(1L);
    }

    @Test
    void getPortfolioByPortfolioId_whenHoldingsExist_returnsPortfolioResponse() {
        List<Holding> holdings = Arrays.asList(holding);

        when(holdingRepository.findByPortfolioId(1L)).thenReturn(holdings);
        when(priceServiceClient.getLatestPrice(1L)).thenReturn(250.0);

        PortfolioResponse result = portfolioService.getPortfolioByPortfolioId(1L);

        assertNotNull(result);
        assertEquals(15000.0, result.getTotalInvestment());
        assertEquals(25000.0, result.getCurrentValue());
        assertEquals(10000.0, result.getOverallPL());

        HoldingResponse holdingResponse = result.getHoldings().get(0);
        assertEquals(1L, holdingResponse.getCompanyId());
        assertEquals("Test Company", holdingResponse.getCompanyName());
        assertEquals(100, holdingResponse.getQuantity());
        assertEquals(150.0, holdingResponse.getAvgPrice());
        assertEquals(250.0, holdingResponse.getCurrentPrice());
        assertEquals(10000.0, holdingResponse.getProfitLoss());

        verify(holdingRepository).findByPortfolioId(1L);
        verify(priceServiceClient).getLatestPrice(1L);
    }

    @Test
    void getPortfolioByPortfolioId_whenNoHoldings_returnsEmptyPortfolioResponse() {
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(Collections.emptyList());

        PortfolioResponse result = portfolioService.getPortfolioByPortfolioId(1L);

        assertNotNull(result);
        assertEquals(0.0, result.getTotalInvestment());
        assertEquals(0.0, result.getCurrentValue());
        assertEquals(0.0, result.getOverallPL());
        assertTrue(result.getHoldings().isEmpty());

        verify(holdingRepository).findByPortfolioId(1L);
        verify(priceServiceClient, never()).getLatestPrice(anyLong());
    }

    @Test
    void buyHolding_whenHoldingDoesNotExist_createsNewHolding() {
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyHolding(buyRequest);

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void buyHolding_whenHoldingExists_updatesExistingHolding() {
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyHolding(buyRequest);

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository).save(holding);

        // Verify updated values
        assertEquals(150, holding.getQuantity()); // 100 + 50
        assertEquals(166.67, holding.getAvgPrice(), 0.01); // Weighted average
        assertEquals(25000.5, holding.getTotalInvested(), 0.01); // 150 * 166.67
    }

    @Test
    void buyHolding_whenZeroQuantity_createsNewHolding() {
        HoldingRequest zeroQuantityRequest = new HoldingRequest(1L, 2L, "New Company", 0, 100.0);
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 2L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyHolding(zeroQuantityRequest);

        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void sellHolding_whenHoldingExistsAndEnoughShares_updatesHolding() {
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.sellHolding(sellRequest);

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository).save(holding);

        // Verify updated values
        assertEquals(70, holding.getQuantity()); // 100 - 30
        assertEquals(10500.0, holding.getTotalInvested()); // 70 * 150.0
    }

    @Test
    void sellHolding_whenHoldingExistsAndAllSharesSold_deletesHolding() {
        HoldingRequest sellAllRequest = new HoldingRequest(1L, 1L, "Test Company", 100, 150.0);
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(holding));

        portfolioService.sellHolding(sellAllRequest);

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository).delete(holding);
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void sellHolding_whenHoldingDoesNotExist_throwsRuntimeException() {
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> portfolioService.sellHolding(sellRequest));

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository, never()).delete(any(Holding.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void sellHolding_whenNotEnoughShares_throwsRuntimeException() {
        HoldingRequest tooManySharesRequest = new HoldingRequest(1L, 1L, "Test Company", 150, 150.0);
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(holding));

        assertThrows(RuntimeException.class, () -> portfolioService.sellHolding(tooManySharesRequest));

        verify(holdingRepository).findByPortfolioIdAndCompanyId(1L, 1L);
        verify(holdingRepository, never()).delete(any(Holding.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void sellHolding_whenExactShares_deletesHolding() {
        HoldingRequest exactSharesRequest = new HoldingRequest(1L, 1L, "Test Company", 100, 150.0);
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(holding));

        portfolioService.sellHolding(exactSharesRequest);

        verify(holdingRepository).delete(holding);
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void buyHolding_withNegativeQuantity_createsNewHolding() {
        HoldingRequest negativeRequest = new HoldingRequest(1L, 3L, "Negative Company", -10, 100.0);
        when(holdingRepository.findByPortfolioIdAndCompanyId(1L, 3L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyHolding(negativeRequest);

        verify(holdingRepository).save(any(Holding.class));
    }
}
