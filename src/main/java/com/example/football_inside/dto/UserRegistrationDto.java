package com.example.football_inside.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
            message = "사용자 이름은 10자 내외의 영문, 숫자, 한글로 이루어져야 합니다.")
    private String username;
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
    private String password;
}