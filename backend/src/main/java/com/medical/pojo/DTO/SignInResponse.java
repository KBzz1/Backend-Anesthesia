package com.medical.pojo.DTO;

public class SignInResponse {
    private String token;

    public SignInResponse() {
    }

    public SignInResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
