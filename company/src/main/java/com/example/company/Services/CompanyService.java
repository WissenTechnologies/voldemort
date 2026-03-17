package com.example.company.Services;

import java.util.List;


import com.example.company.Entities.Company;

public interface CompanyService {
    Company createCompany(Company company);
    List<Company> getAllCompanies();
    Company getCompanyById(int id);
    Company updateCompany(int id, Company company);
    void deleteCompany(int id);
}
