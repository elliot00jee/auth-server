package com.example.user.service;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserAuth;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralAuthenticationException;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;

    private final TokenService tokenService;
    private final UserProfilesService userProfilesService;

    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void createUser(UserAuth userAuth) {
        if(userAuthRepository.findByUserId(userAuth.getUserId()).isPresent()) {
            throw new GeneralBusinessException("중복된 UserId 입니다.");
        }

        userAuth.encryptPassword(passwordEncoder);
        userAuthRepository.save(userAuth);
    }

    public Tokens signin(UserAuth userAuth) {
        Authentication authResult = getAuthenticationResult(userAuth);

        UserProfiles userProfiles = userProfilesService
                .getUserProfilesOptional(getUserIdFromAuthResult(authResult))
                    .orElseThrow(() -> new GeneralAuthenticationException("사용자 프로필 정보가 존재하지 않습니다."));

        return tokenService.generateTokens(userProfiles, false);
    }

    private String getUserIdFromAuthResult(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getUsername();
    }

    private Authentication getAuthenticationResult(UserAuth userAuth) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userAuth.getUserId(), userAuth.getPassword());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (AuthenticationException e) {
            throw new GeneralAuthenticationException("아이디 또는 패스워드가 유효하지 않습니다.");
        }
        return authentication;
    }
}
