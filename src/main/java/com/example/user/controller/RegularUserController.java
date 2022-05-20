package com.example.user.controller;


import com.example.user.controller.dto.AuthDto;
import com.example.user.controller.dto.LoginDto;
import com.example.user.controller.dto.SignupDto;
import com.example.user.entity.Tokens;
import com.example.user.entity.UserAuth;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.service.TokenService;
import com.example.user.service.UserAuthService;
import com.example.user.service.UserProfilesService;
import com.example.user.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RegularUserController {
    private final UserAuthService userAuthService;
    private final UserProfilesService userProfilesService;

    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        Tokens tokens = userAuthService.signin(
                modelMapper.map(loginDto, UserAuth.class)
        );

        response.addCookie(createRefreshTokenCookie(tokens.getRefreshToken()));
        return ResponseUtils.success("accessToken", tokens.getAccessToken());
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        return cookie;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody SignupDto signupDto) {
        userAuthService.createUser(
                modelMapper.map(signupDto, UserAuth.class)
        );
        userProfilesService.createOrUpdateUserProfiles(
                modelMapper.map(signupDto, UserProfiles.class)
        );
        return ResponseUtils.success();
    }
}
