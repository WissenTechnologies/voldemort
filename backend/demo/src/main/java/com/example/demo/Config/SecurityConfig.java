package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

       http
.csrf(csrf -> csrf.disable())
.cors(cors -> {})
.authorizeHttpRequests(auth -> auth

        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/send-otp").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/verify-otp").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/reset-password-with-otp").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/verify-email").permitAll()

        .requestMatchers(HttpMethod.GET, "/auth/users").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, "/auth/users").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/auth/users/**").hasRole("ADMIN")
        .anyRequest().authenticated()

)

.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtFilter jwtFilter(){
      return new JwtFilter();
    }
}
