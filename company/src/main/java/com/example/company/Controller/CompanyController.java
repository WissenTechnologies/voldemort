package com.example.company.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.company.Entities.Company;
import com.example.company.Services.CompanyService;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    // CREATE COMPANY
    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        if(companyService.existsById(company.getId())){
            return new ResponseEntity<>(company, HttpStatus.CONFLICT);
        }
        Company createdCompany = companyService.createCompany(company);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    // GET ALL COMPANIES
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }

    // GET COMPANY BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable int id) {
        try {
            Company company = companyService.getCompanyById(id);
            return new ResponseEntity<>(company, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
        }
    }

    // UPDATE COMPANY
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable int id, @RequestBody Company company) {
        try {
            company.setValue(null);
            company.setVolume(null);
            Company updated = companyService.updateCompany(id, company);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
        }
    }

    // DELETE COMPANY
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable int id) {
        try {
            companyService.deleteCompany(id);
            return new ResponseEntity<>("Company deleted successfully", HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
        }
    }
}
