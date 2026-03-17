package com.example.auth.payload.response;

import java.util.List;

public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = ""; // Add refresh token later if needed
        this.user = new UserDto(id, username, email, roles);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public static class UserDto {
        private String id;
        private String name;
        private String email;
        private List<String> roles;

        public UserDto(Long id, String name, String email, List<String> roles) {
            this.id = String.valueOf(id);
            this.name = name;
            this.email = email;
            this.roles = roles;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
