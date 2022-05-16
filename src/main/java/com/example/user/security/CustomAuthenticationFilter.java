package com.example.user.security;

import com.example.user.controller.dto.AuthDto;
import com.example.user.controller.dto.LoginDto;
import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralAuthenticationException;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.service.TokenService;
import com.example.user.service.UserProfilesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final UserProfilesService userProfilesService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      ObjectMapper objectMapper,
                                      TokenService tokenService,
                                      UserProfilesService userProfilesService) {
        super(authenticationManager);
        this.objectMapper = objectMapper;
        this.tokenService = tokenService;
        this.userProfilesService = userProfilesService;

        setFilterProcessesUrl("/signin");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        LoginDto loginDto;
        try {
            loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        } catch (IOException exception) {
            throw new GeneralBusinessException("유효하지 않은 Request Body");
        }

        if(!userProfilesService.isRegisteredUser(loginDto.getUserId())) {
            throw new GeneralAuthenticationException("존재하지 않는 사용자");
        }

        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUserId(),
                        loginDto.getPassword(),
                        new ArrayList<>())
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException, ServletException {
        System.out.println("CustomAuthenticationFilter.successfulAuthentication");
        String username = ((User) authentication.getPrincipal()).getUsername();

        UserProfiles userProfiles = UserProfiles.builder()
                .userId(username)
                .build();

        Tokens tokens = tokenService.generateTokens(userProfiles, false);

        AuthDto authDto = new AuthDto();
        authDto.setAccesstoken(tokens.getAccessToken());

        response.setContentType("application/json; charset=UTF-8");

        response.addCookie(createRefreshTokenCookie(tokens.getRefreshToken()));
        response.getWriter().write(objectMapper.writeValueAsString(authDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        throw new GeneralAuthenticationException(failed.getMessage());
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
