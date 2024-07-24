package com.example.football_inside.controller;

import com.example.football_inside.dto.UserRegistrationDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        User registeredUser = userService.registerNewUser(registrationDto);

        // 만약 registeredUser가 null이라면, 이미 존재하는 사용자라는 의미
        if (registeredUser == null) {
            return ResponseEntity.badRequest().body("User already exists");
        } else {
            return ResponseEntity.ok("User registered successfully");
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) { // 사용자 이름이 이미 존재하는 지 확인
        return ResponseEntity.ok(userService.isUsernameAvailable(username));
    }
}