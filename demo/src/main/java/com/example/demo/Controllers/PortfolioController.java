package com.example.demo.Controllers;

import com.example.demo.Entities.Portfolio;
import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Services.PortfolioService;
import com.example.demo.dto.TradeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/portfolios")
@CrossOrigin("*")
@Tag(name = "Portfolios", description = "Portfolio management and trading APIs")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private AuthRepo userRepo;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow();
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    @GetMapping
    @Operation(summary = "Get user portfolios", description = "Retrieves all portfolios owned by the authenticated user")
    public List<Portfolio> getPortfolios() {
        return portfolioService.getPortfoliosByUser(getCurrentUser());
    }

    @GetMapping("/all")
    @Operation(summary = "Admin: Get all portfolios", description = "Admin-only: retrieves all portfolios with holdings")
    public ResponseEntity<?> getAllPortfoliosAdmin() {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(portfolioService.getAllPortfolios());
    }

    @PostMapping
    @Operation(summary = "Create portfolio", description = "Creates a new empty portfolio for the user")
    public Portfolio createPortfolio(@RequestBody Portfolio portfolio) {
        portfolio.setUser(getCurrentUser());
        if (portfolio.getAvailableCash() == null) {
            portfolio.setAvailableCash(BigDecimal.ZERO);
        }
        return portfolioService.createPortfolio(portfolio);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update portfolio", description = "Updates a portfolio's name or available cash")
    public Portfolio updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolioDetails) {
        User currentUser = getCurrentUser();
        Portfolio portfolio = portfolioService.getPortfolioWithUserById(id)
            .orElseThrow();
        if (!isAdmin(currentUser) && (portfolio.getUser() == null || !portfolio.getUser().getId().equals(currentUser.getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        portfolio.setName(portfolioDetails.getName());
        portfolio.setAvailableCash(portfolioDetails.getAvailableCash() == null ? BigDecimal.ZERO : portfolioDetails.getAvailableCash());
        return portfolioService.createPortfolio(portfolio);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete portfolio", description = "Deletes a portfolio by ID")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Portfolio portfolio = portfolioService.getPortfolioWithUserById(id).orElseThrow();
        if (!isAdmin(currentUser) && (portfolio.getUser() == null || !portfolio.getUser().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        portfolioService.deletePortfolio(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/trade")
    @Operation(summary = "Execute trade", description = "Executes a BUY or SELL order for a stock in the portfolio")
    public ResponseEntity<?> trade(@PathVariable Long id, @RequestBody TradeRequest request) {
        try {
            User currentUser = getCurrentUser();
            Portfolio updated;
            if ("BUY".equalsIgnoreCase(request.getType())) {
                updated = portfolioService.buyStock(id, request.getSymbol(), request.getQuantity(), currentUser);
            } else {
                updated = portfolioService.sellStock(id, request.getSymbol(), request.getQuantity(), currentUser);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
