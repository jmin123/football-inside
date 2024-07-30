package com.example.football_inside.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String email;
    private String username;

    public LoginResponse(String token, String email, String username) {
        this.token = token;
        this.email = email;
        this.username = username;
    }
}