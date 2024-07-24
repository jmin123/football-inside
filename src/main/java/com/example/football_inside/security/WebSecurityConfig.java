package com.example.football_inside.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;

    // UserDetailsService를 주입하는 생성자
    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Spring Security의 필터 체인을 설정하는 메서드
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // HTTP 요청에 대한 인증 및 권한 설정
                .authorizeHttpRequests((requests) -> requests
                        // 로그인 페이지와 리소스 디렉토리는 모든 사용자에게 허용
                        .requestMatchers("/login", "/resources/**").permitAll()
                        // 그 외의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin((form) -> form
                        // 커스텀 로그인 페이지 설정
                        .loginPage("/login")
                        // 로그인 페이지 접근 모든 사용자 허용
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(LogoutConfigurer::permitAll);

        // 설정을 마친 HttpSecurity 객체를 빌드하여 반환
        return http.build();
    }

    // 비밀번호 인코더를 설정하는 메서드
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder를 사용하여 비밀번호를 인코딩
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider를 설정하는 메서드
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 사용자 정보를 제공하는 UserDetailsService 설정
        authProvider.setUserDetailsService(userDetailsService);
        // 비밀번호 인코더 설정
        authProvider.setPasswordEncoder(passwordEncoder());
        // 설정을 마친 DaoAuthenticationProvider 객체를 반환
        return authProvider;
    }
}
