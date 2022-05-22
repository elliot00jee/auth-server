package com.example.user.controller;

import com.example.user.controller.dto.AuthDto;
import com.example.user.controller.dto.SignupDto;
import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.service.TokenService;
import com.example.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/signup")
    public ResponseEntity<?> createKeycloakUser(@RequestBody SignupDto signupDto) {
        UserProfiles userProfiles = userProfilesService.getUserProfiles(signupDto.getUserId());

        if(userProfiles.getRole() != null && !userProfiles.getRole().isEmpty()) {
            throw new GeneralBusinessException("이미 등록된 사용자입니다.");
        }

        userProfiles.setRole(signupDto.getRole());

        userProfilesService.createOrUpdateUserProfiles(userProfiles);

        Tokens tokens = tokenService.generateTokens(userProfiles, false);

        AuthDto authDto = new AuthDto();
        authDto.setAccesstoken(tokens.getAccessToken());

        addRefreshTokenCookie(tokens.getRefreshToken());
        return success(authDto);
    }
}
