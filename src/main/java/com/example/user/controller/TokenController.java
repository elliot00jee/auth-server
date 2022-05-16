package com.example.user.controller;


import com.example.user.controller.dto.AuthDto;
import com.example.user.entity.Tokens;
import com.example.user.service.TokenService;
import com.example.user.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TokenController {
    private final TokenService tokenService;

    @GetMapping("/tokens/{code}")
    public ResponseEntity<?> getTokens(@PathVariable String code, HttpServletResponse response) {
        Tokens tokens = tokenService.getTokens(code);

        AuthDto authDto = new AuthDto();
        authDto.setAccesstoken(tokens.getAccessToken());

        Cookie cookie = new Cookie("refresh_token", tokens.getRefreshToken());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseUtils.success(authDto);
    }
}
