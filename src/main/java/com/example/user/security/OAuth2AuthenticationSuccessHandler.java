package com.example.user.security;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.service.TokenService;
import com.example.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final String FRONT_LOGINPAGE_URL = "http://localhost:8080";
    private final String FRONT_SIGNUPPAGE_URL = "http://localhost:8080";
    private final TokenService tokenService;
    private final UserProfilesService userProfilesService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess");

        OidcUser user = ((OidcUser) authentication.getPrincipal());
        Map<String, Object> attributes = user.getAttributes();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", String.valueOf(attributes.get("preferred_username")));

        UserProfiles keycloakUserProfiles = UserProfiles.builder()
                .userId(String.valueOf(attributes.get("preferred_username")))
                .department(String.valueOf(attributes.get("department")))
                .username(String.valueOf(attributes.get("name")))
                .build();


        UserProfiles savedUserProfiles = userProfilesService.getUserProfilesOptional(keycloakUserProfiles.getUserId())
                        .orElseGet(UserProfiles::new);

        savedUserProfiles.updateUserProfiles(keycloakUserProfiles);

        userProfilesService.createOrUpdateUserProfiles(savedUserProfiles);

        Tokens tokens = tokenService.generateTokens(savedUserProfiles, true);

        String userRole = savedUserProfiles.getRole();
        String redirectUrl = userRole == null ? FRONT_SIGNUPPAGE_URL : FRONT_LOGINPAGE_URL;
        response.sendRedirect(UriComponentsBuilder.fromHttpUrl(redirectUrl)
                .queryParam("code", tokens.getJwtId())
                .queryParam("userId", keycloakUserProfiles.getUserId())
                .build().toString());
    }
}
