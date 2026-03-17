package com.example.company.Repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.company.Entities.Company;

public interface CompanyRepo extends JpaRepository<Company,Integer> {
    
}
