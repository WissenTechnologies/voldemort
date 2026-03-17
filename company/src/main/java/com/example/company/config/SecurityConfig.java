package com.example.company.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.company.security.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtFilter jwtFilter(){
        return new JwtFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth

                // READ - USER + ADMIN
                .requestMatchers(HttpMethod.GET, "/api/companies/**")
                .hasAnyRole("USER","ADMIN")

                // CREATE UPDATE DELETE - ADMIN ONLY
                .requestMatchers(HttpMethod.POST, "/api/companies/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT, "/api/companies/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/companies/**")
                .hasRole("ADMIN")

                .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
