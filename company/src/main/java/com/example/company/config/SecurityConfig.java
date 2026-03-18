package com.example.company.config;

import java.util.List;

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
        .cors(cors -> {})
        .authorizeHttpRequests(auth -> auth

                // GET ALL - /api/companies
                .requestMatchers(HttpMethod.GET, "/api/companies")
                .permitAll()

                // GET BY ID - /api/companies/{id}
                .requestMatchers(HttpMethod.GET, "/api/companies/**")
                .permitAll()

                // CREATE - ADMIN ONLY
                .requestMatchers(HttpMethod.POST, "/api/companies")
                .hasRole("ADMIN")

                // UPDATE - ADMIN ONLY
                .requestMatchers(HttpMethod.PUT, "/api/companies/**")
                .hasRole("ADMIN")

                // DELETE - ADMIN ONLY
                .requestMatchers(HttpMethod.DELETE, "/api/companies/**")
                .hasRole("ADMIN")

                .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

@Bean
public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {

    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

    config.setAllowedOrigins(List.of("http://localhost:3000")); // React
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
            new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", config);

    return source;
}
}
