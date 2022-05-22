package com.example.user.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SignupDto {
    private String userId;
    private String password;
    private String username;
    private String department;
    // [TODO: enum 으로 바꾸기]
    private String role;

}
