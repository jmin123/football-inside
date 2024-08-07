package com.example.football_inside;

import com.example.football_inside.dto.LoginDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        User testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);
    }

    @AfterEach
    public void cleanup() {
        userRepository.delete(userRepository.findByEmail(TEST_EMAIL).orElseThrow());
    }

    @Test
    public void testLoginRateLimit() throws Exception {
        LoginDto loginDto = new LoginDto(TEST_EMAIL, "wrongpassword", false);
        String content = new ObjectMapper().writeValueAsString(loginDto);

        // 첫 5번의 잘못된 비밀번호 입력은 허용
        for (int i = 0; i < 5; i++) {
            final int requestNumber = i;
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .with(request -> {
                                request.setRemoteAddr("192.168.1.1");
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid password")))
                    .andDo(result -> System.out.println("Response " + requestNumber + ": " + result.getResponse().getContentAsString()));
        }

        // 6번째가 들어오면 rate limit에 걸림
        MvcResult rateLimitedResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.1");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value(containsString("Rate limit exceeded")))
                .andReturn();

        System.out.println("Rate limited response: " + rateLimitedResult.getResponse().getContentAsString());

        // 1분 기다리고
        Thread.sleep(60000);

        //  이제 기다리면 다시 5번 시도 가능
        for (int i = 0; i < 5; i++) {
            final int requestNumber = i;
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .with(request -> {
                                request.setRemoteAddr("192.168.1.1");
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid password")))
                    .andDo(result -> System.out.println("Response after wait " + requestNumber + ": " + result.getResponse().getContentAsString()));
        }

        // 그 다음에 또 그러면 rate limit에 걸림
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.1");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value(containsString("Rate limit exceeded")));
    }
}