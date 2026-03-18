package com.example.demo.Config;

import com.example.demo.Entities.Stock;
import com.example.demo.Entities.StockHistory;
import com.example.demo.Entities.User;
import com.example.demo.Entities.Role;
import com.example.demo.Services.StockService;
import com.example.demo.Repository.AuthRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StockService stockService;

    @Autowired
    private AuthRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting data initialization...");
        
        if (stockService.getAllStocks().isEmpty()) {
            seedStocks();
        }
        
        try {
            seedAdminUser();
        } catch (Exception e) {
            System.err.println("Error creating users: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Data initialization completed.");
    }

    private void seedStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("TSLA", "Tesla Inc.", new BigDecimal("20945.25"), new BigDecimal("21231.40"), new BigDecimal("20600.60"), "118.5M", "802.3B"));
         stocks.add(new Stock("AAPL", "Apple Inc.", new BigDecimal("14810.85"), new BigDecimal("15000.00"), new BigDecimal("14700.00"), "85.2M", "2.8T"));
        stocks.add(new Stock("MSFT", "Microsoft Corp.", new BigDecimal("34251.61"), new BigDecimal("34936.64"), new BigDecimal("33566.58"), "50.2M", "2.5T"));
        stocks.add(new Stock("AMZN", "Amazon.com Inc.", new BigDecimal("12061.56"), new BigDecimal("12302.79"), new BigDecimal("11820.33"), "60.1M", "1.2T"));
        stocks.add(new Stock("NVDA", "NVIDIA Corp.", new BigDecimal("72648.24"), new BigDecimal("74000.00"), new BigDecimal("71000.00"), "45.5M", "1.1T"));
        stocks.add(new Stock("META", "Meta Platforms", new BigDecimal("40274.09"), new BigDecimal("41000.00"), new BigDecimal("39500.00"), "35.2M", "750B"));
        stocks.add(new Stock("NFLX", "Netflix Inc.", new BigDecimal("36990.61"), new BigDecimal("37500.00"), new BigDecimal("36000.00"), "25.2M", "250B"));
        stocks.add(new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("11583.48"), new BigDecimal("11800.00"), new BigDecimal("11400.00"), "30.1M", "1.5T"));
        stocks.add(new Stock("RELIANCE", "Reliance Industries", new BigDecimal("2845.50"), new BigDecimal("2900.00"), new BigDecimal("2800.00"), "10.2M", "19T"));
        stocks.add(new Stock("TCS", "Tata Consultancy Services", new BigDecimal("3950.25"), new BigDecimal("4050.00"), new BigDecimal("3900.00"), "2.5M", "14T"));
        stocks.add(new Stock("HDFCBANK", "HDFC Bank Ltd.", new BigDecimal("1450.75"), new BigDecimal("1480.00"), new BigDecimal("1420.00"), "18.5M", "11T"));
        stocks.add(new Stock("INFY", "Infosys Ltd.", new BigDecimal("1620.40"), new BigDecimal("1650.00"), new BigDecimal("1590.00"), "5.8M", "6T"));

        for (Stock stock : stocks) {
            stockService.saveStock(stock);
            seedHistory(stock);
        }
    }

    private final Random random = new Random();

    private void seedHistory(Stock stock) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal currentPrice = stock.getCurrentPrice();
        
        for (int i = 60; i >= 0; i--) {
            BigDecimal variation = currentPrice.multiply(BigDecimal.valueOf((random.nextDouble() - 0.5) * 0.02)); 
            BigDecimal historyPrice = currentPrice.add(variation).setScale(2, BigDecimal.ROUND_HALF_UP);
            
            StockHistory history = new StockHistory(stock, now.minusMinutes(i), historyPrice);
            stockService.saveHistory(List.of(history));
        }
    }

    private void seedAdminUser() {
        System.out.println("Starting user seeding...");
        
        // Create admin user
        if (!userRepository.existsByEmail("admin@voldemort.com")) {
            System.out.println("Creating admin user...");
            User admin = new User();
            admin.setEmail("admin@voldemort.com");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
            System.out.println("Email: admin@voldemort.com");
            System.out.println("Password: admin123");
        } else {
            System.out.println("Admin user already exists.");
        }

        // Create normal user
        if (!userRepository.existsByEmail("user@voldemort.com")) {
            System.out.println("Creating normal user...");
            User user = new User();
            user.setEmail("user@voldemort.com");
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setEnabled(true);
            
            userRepository.save(user);
            System.out.println("Normal user created successfully!");
            System.out.println("Email: user@voldemort.com");
            System.out.println("Password: user123");
        } else {
            System.out.println("Normal user already exists.");
        }
    }
}
