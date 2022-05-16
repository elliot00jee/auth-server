package com.example.user.controller;


import com.example.user.controller.dto.AuthDto;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserAuthService userAuthService;
    private final UserProfilesService userProfilesService;
    private final TokenService tokenService;

    private final ModelMapper modelMapper;

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

    @PostMapping("/signup/keycloak")
    public ResponseEntity<?> createKeycloakUser(@RequestBody SignupDto signupDto, HttpServletResponse response) {
        UserProfiles userProfiles = userProfilesService.getUserProfiles(signupDto.getUserId());

        if(userProfiles.getRole() != null && !userProfiles.getRole().isEmpty()) {
            throw new GeneralBusinessException("이미 등록된 사용자입니다.");
        }

        userProfiles.setRole(signupDto.getRole());

        userProfilesService.createOrUpdateUserProfiles(userProfiles);

        Tokens tokens = tokenService.generateTokens(userProfiles, false);

        AuthDto authDto = new AuthDto();
        authDto.setAccesstoken(tokens.getAccessToken());

        Cookie cookie = new Cookie("refresh_token", tokens.getRefreshToken());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseUtils.success(authDto);
    }

    @GetMapping("/authtest")
    public String authTest(@AuthenticationPrincipal Object object) {
        return "authTest";
    }
}
