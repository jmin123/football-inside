package com.example.football_inside.service;

import com.example.football_inside.dto.UserRegistrationDto;
import com.example.football_inside.entity.User;

public interface UserService {
    User registerNewUser(UserRegistrationDto registrationDto);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
}