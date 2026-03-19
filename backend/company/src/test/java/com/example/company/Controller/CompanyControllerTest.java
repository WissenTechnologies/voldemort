package com.example.company.Controller;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.company.Entities.Company;
import com.example.company.Services.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @Test
    void createCompany_whenValidRequest_returnsCreatedCompany() throws Exception {
        Company request = new Company();
        request.setId(1);
        request.setCompanyName("Acme");

        Company created = new Company();
        created.setId(1);
        created.setCompanyName("Acme");

        when(companyService.createCompany(any(Company.class))).thenReturn(created);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Acme"));

        verify(companyService).createCompany(any(Company.class));
    }

    @Test
    void getAllCompanies_whenCompaniesExist_returnsList() throws Exception {
        Company c1 = new Company();
        c1.setId(1);
        c1.setCompanyName("Acme");

        Company c2 = new Company();
        c2.setId(2);
        c2.setCompanyName("Globex");

        when(companyService.getAllCompanies()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Acme"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].companyName").value("Globex"));

        verify(companyService).getAllCompanies();
    }

    @Test
    void getCompanyById_whenFound_returnsCompany() throws Exception {
        Company c = new Company();
        c.setId(10);
        c.setCompanyName("Acme");

        when(companyService.getCompanyById(10)).thenReturn(c);

        mockMvc.perform(get("/api/companies/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.companyName").value("Acme"));

        verify(companyService).getCompanyById(10);
    }

    @Test
    void getCompanyById_whenNotFound_returnsNotFoundMessage() throws Exception {
        when(companyService.getCompanyById(99)).thenThrow(new RuntimeException("Company not found"));

        mockMvc.perform(get("/api/companies/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Company not found"));
    }

    @Test
    void updateCompany_whenFound_returnsUpdatedCompany() throws Exception {
        Company request = new Company();
        request.setCompanyName("New Name");
        request.setSymbol("NEW");
        request.setVolume(999L);
        request.setValue(123.45);

        Company updated = new Company();
        updated.setId(5);
        updated.setCompanyName("New Name");
        updated.setSymbol("NEW");
        updated.setVolume(10L);
        updated.setValue(20.0);

        when(companyService.updateCompany(eq(5), any(Company.class))).thenReturn(updated);

        mockMvc.perform(put("/api/companies/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.companyName").value("New Name"))
                .andExpect(jsonPath("$.symbol").value("NEW"));

        verify(companyService).updateCompany(eq(5), any(Company.class));
    }

    @Test
    void updateCompany_whenNotFound_returnsNotFoundMessage() throws Exception {
        when(companyService.updateCompany(eq(123), any(Company.class)))
                .thenThrow(new RuntimeException("Company not found"));

        mockMvc.perform(put("/api/companies/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Company())))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Company not found"));
    }

    @Test
    void deleteCompany_whenFound_returnsNoContent() throws Exception {
        doNothing().when(companyService).deleteCompany(7);

        mockMvc.perform(delete("/api/companies/{id}", 7))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(7);
    }

    @Test
    void deleteCompany_whenNotFound_returnsNotFoundMessage() throws Exception {
        doThrow(new RuntimeException("Company not found")).when(companyService).deleteCompany(7);

        mockMvc.perform(delete("/api/companies/{id}", 7))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Company not found"));
    }
}

