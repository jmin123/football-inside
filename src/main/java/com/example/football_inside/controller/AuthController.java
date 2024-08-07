package com.example.football_inside.controller;

import com.example.football_inside.annotation.RateLimit;
import com.example.football_inside.dto.LoginDto;
import com.example.football_inside.dto.UserRegistrationDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.response.LoginResponse;
import com.example.football_inside.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    // 로그인
    @RateLimit
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = userService.loginUser(
                    loginDto.getEmail(), loginDto.getPassword(), loginDto.isRememberMe());

            // refresh token을 HTTP-only로 해서 cookie 보호
            Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true); // HTTPS 온리
            refreshTokenCookie.setPath("/api/auth/refresh"); // cookie가 refresh endpoint로 가는 것을 제한
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshTokenCookie);

            // 이후에 response body로부터 제거
            loginResponse.setRefreshToken(null);

            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            log.error("Login failed for user: {}", loginDto.getEmail(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User registeredUser = userService.registerNewUser(registrationDto);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body("예상치 못한 오류가 발생했습니다. 다시 시도해 주세요.");
        }
    }

    // 사용자 이름이 이미 존재하는 지 확인
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.isUsernameAvailable(username));
    }

    // 이메일이 이미 존재하는 지 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.isEmailAvailable(email));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
        }
        try {
            String newAccessToken = userService.refreshAccessToken(refreshToken);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", newAccessToken);
            return ResponseEntity.ok(tokenMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}