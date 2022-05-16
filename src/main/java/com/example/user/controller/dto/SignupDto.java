package com.example.user.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    private String userId;
    private String password;
    private String username;
    private String department;
    // [TODO: enum 으로 바꾸기]
    private String role;

}
