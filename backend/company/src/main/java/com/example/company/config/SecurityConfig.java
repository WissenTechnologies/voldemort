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
        .cors(cors -> {})
        .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
 
                .requestMatchers(HttpMethod.GET, "/api/companies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
 
                .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/companies/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/companies/**").permitAll()
 
                .anyRequest().authenticated()
        )
 
        // 🔥 ADD THIS BLOCK
        .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.getWriter().write("Unauthorized - Token missing/invalid");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.getWriter().write("Forbidden - ADMIN role required");
                })
        )
 
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
 
    return http.build();
}
}
