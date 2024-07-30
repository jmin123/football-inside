package com.example.football_inside.service;

import com.example.football_inside.dto.UserRegistrationDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.response.LoginResponse;

public interface UserService {
    User registerNewUser(UserRegistrationDto registrationDto);
    LoginResponse loginUser(String email, String password);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
}