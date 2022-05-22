package com.example.user.controller;

import com.example.user.controller.dto.AuthDto;
import com.example.user.controller.dto.SignupDto;
import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.service.TokenService;
import com.example.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.user.util.ResponseUtils.addRefreshTokenCookie;
import static com.example.user.util.ResponseUtils.success;

@RequiredArgsConstructor
@RestController
@RequestMapping("/keycloak")
public class KeycloakUserController {
    private final UserProfilesService userProfilesService;
    private final TokenService tokenService;
    private final ModelMapper modelMapper;

    @PostMapping("/signup")
    public ResponseEntity<?> createKeycloakUser(@RequestBody SignupDto signupDto) {
        UserProfiles userProfiles = userProfilesService.createKeycloakUser(
                modelMapper.map(signupDto, UserProfiles.class)
        );

        Tokens tokens = tokenService.generateTokens(userProfiles, false);

        AuthDto authDto = new AuthDto();
        authDto.setAccesstoken(tokens.getAccessToken());

        addRefreshTokenCookie(tokens.getRefreshToken());
        return success(authDto);
    }
}
