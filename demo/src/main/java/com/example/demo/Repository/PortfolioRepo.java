package com.example.demo.Repository;

import com.example.demo.Entities.Portfolio;
import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepo extends JpaRepository<Portfolio, Long> {
    @EntityGraph(attributePaths = {"holdings"})
    List<Portfolio> findByUser(User user);

    @EntityGraph(attributePaths = {"holdings"})
    Optional<Portfolio> findWithHoldingsById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Portfolio> findWithUserById(Long id);

    @EntityGraph(attributePaths = {"user", "holdings"})
    Optional<Portfolio> findWithUserAndHoldingsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"holdings"})
    List<Portfolio> findAll();
}
