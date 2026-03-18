package com.example.company.Services;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.company.Entities.Company;
import com.example.company.Repos.CompanyRepo;

@Service
public class CompanyServiceImp implements CompanyService {

    @Autowired
    private  CompanyRepo repository;
    
    @Override
    public Company createCompany(Company company) {
        return repository.save(company);
    }
    @Override
    public List<Company> getAllCompanies() {
        return repository.findAll();
    }
    @Override
    public Company getCompanyById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }
    @Override
public Company updateCompany(int id, Company updatedCompany) {

    Company existing = getCompanyById(id);

    // BASIC
    existing.setCompanyName(updatedCompany.getCompanyName());
    existing.setSymbol(updatedCompany.getSymbol());
    existing.setSector(updatedCompany.getSector());
    existing.setIndustry(updatedCompany.getIndustry());

    // DETAILS
    existing.setCeo(updatedCompany.getCeo());
    existing.setFoundedYear(updatedCompany.getFoundedYear());
    existing.setHeadquarters(updatedCompany.getHeadquarters());

    // FINANCIAL
    existing.setMarketCap(updatedCompany.getMarketCap());
    existing.setPeRatio(updatedCompany.getPeRatio());
    existing.setEps(updatedCompany.getEps());

    // EXTRA
    existing.setDescription(updatedCompany.getDescription());

    // ❌ DO NOT TOUCH
    // existing.setValue(...)
    // existing.setVolume(...)

    return repository.save(existing);
}
    @Override
    public void deleteCompany(int id) {
        repository.deleteById(id);
    }
    @Override
    public Boolean existsById(int id) {
        return repository.existsById(id);
    }
}
