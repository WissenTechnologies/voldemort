package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {
 
    String header = request.getHeader("Authorization");
    String path = request.getRequestURI();
 
    if (header != null && header.startsWith("Bearer ")) {
 
        String token = header.substring(7);
 
        try {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            System.out.println("[JwtFilter] Path: " + path + ", Email: " + email + ", Role from token: " + role);
 
            String authority = "ROLE_" + (role != null ? role.toUpperCase() : "USER");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );
            
            System.out.println("[JwtFilter] Granted authority: " + authority);
            System.out.println("[JwtFilter] Is authenticated: " + authentication.isAuthenticated());
 
            SecurityContextHolder.getContext().setAuthentication(authentication);
 
        } catch (Exception e) {
            System.out.println("[JwtFilter] Token validation failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Token: " + e.getMessage());
            return;
        }
    } else {
        System.out.println("[JwtFilter] No Bearer token found for path: " + path);
    }
 
    filterChain.doFilter(request, response);
}
}