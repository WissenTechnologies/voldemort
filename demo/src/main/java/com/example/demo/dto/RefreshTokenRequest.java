package com.example.demo.dto;

public class RefreshTokenRequest {
    private String refresh_token;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
