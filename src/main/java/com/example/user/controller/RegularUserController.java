package com.example.user.controller;


import com.example.user.controller.dto.LoginDto;
import com.example.user.controller.dto.SignupDto;
import com.example.user.entity.Tokens;
import com.example.user.entity.UserAuth;
import com.example.user.entity.UserProfiles;
import com.example.user.service.UserAuthService;
import com.example.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.example.user.util.ResponseUtils.addRefreshTokenCookie;
import static com.example.user.util.ResponseUtils.success;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RegularUserController {
    private final UserAuthService userAuthService;
    private final UserProfilesService userProfilesService;

    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginDto loginDto) {
        Tokens tokens = userAuthService.signin(
                modelMapper.map(loginDto, UserAuth.class)
        );

        addRefreshTokenCookie(tokens.getRefreshToken());
        return success("accessToken", tokens.getAccessToken());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody SignupDto signupDto) {
        userAuthService.createUser(
                modelMapper.map(signupDto, UserAuth.class)
        );
        userProfilesService.createOrUpdateUserProfiles(
                modelMapper.map(signupDto, UserProfiles.class)
        );
        return success();
    }
}
