package com.example.football_inside.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    @Pattern(regexp = "^[a-zA-Z0-9]{1,10}$",
            message = "사용자 이름은 10자 내외의 문자와 숫자로 이루어져야 합니다.")
    private String username;
    private String email;
    private String password;
}
