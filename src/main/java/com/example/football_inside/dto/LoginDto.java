package com.example.football_inside.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    @NotBlank(message = "이메일이 필요합니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "비밀번호가 필요합니다.")
    private String password;
    private boolean rememberMe;

    public LoginDto(String mail, String password, boolean b) {
        this.email = mail;
        this.password = password;
        this.rememberMe = b;
    }
}
