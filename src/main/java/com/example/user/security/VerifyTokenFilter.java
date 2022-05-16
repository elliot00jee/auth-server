package com.example.user.security;

import com.example.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class VerifyTokenFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if(header != null) {
            String accessToken = header.replace("Bearer ", "");

            Map<String, Object> userInfo = tokenService.extractUserInfoFromToken(accessToken);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userInfo, accessToken, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
