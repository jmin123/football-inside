package com.example.football_inside.service;

import com.example.football_inside.dto.UserRegistrationDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.repository.UserRepository;
import com.example.football_inside.response.LoginResponse;
import com.example.football_inside.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("로그인 정보를 찾을 수 없습니다.");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, user.getEmail(), user.getUsername());
    }

    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (!isUsernameAvailable(registrationDto.getUsername())) {
            throw new RuntimeException("이미 사용 중인 사용자 이름입니다.");
        }
        if (!isEmailAvailable(registrationDto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }
}