package com.example.demo.Controllers;

import java.util.List;
import java.util.Map;

import com.example.demo.Entities.Company;
import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Services.CompanyService;
import com.example.demo.dto.IssueSharesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/companies")
@CrossOrigin("*")
@Tag(name = "Companies", description = "Company registry and issuance APIs")
public class CompaniesController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CompanyService companyService;

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
    @Operation(summary = "List companies", description = "Returns all companies in the registry")
    public List<Company> listCompanies() {
        return companyService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company", description = "Returns a company by id")
    public ResponseEntity<?> getCompany(@PathVariable Long id) {
        return companyService.getById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Admin: create company", description = "Admin-only: creates a new company and syncs it to stock listings")
    public ResponseEntity<?> createCompany(@RequestBody Company company) {
        try {
            User currentUser = getCurrentUser();
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            return ResponseEntity.ok(companyService.create(company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Admin: update company", description = "Admin-only: updates company details and syncs it to stock listings")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody Company patch) {
        try {
            User currentUser = getCurrentUser();
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            return ResponseEntity.ok(companyService.update(id, patch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Admin: delete company", description = "Admin-only: deletes a company and removes its stock listing")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            companyService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/issue-shares")
    @Operation(summary = "Admin: issue shares", description = "Admin-only: increases total/available shares for a company")
    public ResponseEntity<?> issueShares(@PathVariable Long id, @RequestBody IssueSharesRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            return ResponseEntity.ok(companyService.issueShares(id, request.getQuantity()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestParam("query") String query) {
        String url = UriComponentsBuilder
                .fromUriString("https://autocomplete.clearbit.com/v1/companies/suggest")
                .queryParam("query", query)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
    }
}
