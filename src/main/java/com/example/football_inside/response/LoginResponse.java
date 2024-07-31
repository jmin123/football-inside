package com.example.football_inside.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String email;
    private String username;
    private String profilePicture;
    private Set<String> roles;
    private String token;
}