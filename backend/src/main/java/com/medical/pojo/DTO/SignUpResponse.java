package com.medical.pojo.DTO;

public class SignUpResponse {
    private String token;

    public SignUpResponse() {
    }

    public SignUpResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
