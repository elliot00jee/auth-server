package com.example.user.service;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.UnauthenticatedException;
import com.example.user.security.AbstractJwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class TokenService {
    private static final Long ACCESSTOKEN_VALID_SECONDS = 30L * 60L;
    private static final Long REFRESHTOKEN_VALID_SECONDS = 24 * 60L * 60L;

    private static final Long ONETIMECODE_REDIS_TIMEOUT_SECONDS = 5 * 60L;
    private static final Long TOKENS_REDIS_TIMEOUT_SECONDS = REFRESHTOKEN_VALID_SECONDS;

    private final AbstractJwtService jwtService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public Tokens generateTokens(UserProfiles userProfiles, boolean needOneTimeCode) {
        String jwtId = UUID.randomUUID().toString();

        Tokens tokens = Tokens.builder()
                .jwtId(jwtId)
                .userId(userProfiles.getUserId())
                .accessToken(generateAccessToken(jwtId, createUserProfilesClaim(userProfiles)))
                .refreshToken(generateRefreshToken())
                .isOneTimeCodeValid(needOneTimeCode)
                .build();

        updateTokenToRedis(tokens);

        return tokens;
    }

    private Map<String, Object> createUserProfilesClaim(UserProfiles userProfiles) {
        Map<String, Object> userProfilesClaim = new HashMap<>();
        userProfilesClaim.put("userId", userProfiles.getUserId());
        userProfilesClaim.put("role", userProfiles.getRole());

        return userProfilesClaim;
    }

    private void updateTokenToRedis(Tokens tokens) {
        long timeoutSeconds = tokens.isOneTimeCodeValid() ? ONETIMECODE_REDIS_TIMEOUT_SECONDS : TOKENS_REDIS_TIMEOUT_SECONDS;
        try {
            redisService.update(tokens.getJwtId(), objectMapper.writeValueAsString(tokens), timeoutSeconds);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public String generateAccessToken(String jwtId, Map<String, Object> userProfilesClaim) {
        return jwtService.generateToken(jwtId, userProfilesClaim, getExpirationTime(ACCESSTOKEN_VALID_SECONDS));
    }

    public String generateRefreshToken() {
        return jwtService.generateToken(null, null, getExpirationTime(REFRESHTOKEN_VALID_SECONDS));
    }

    public Tokens getTokensByOneTimeCode(String code) {
        String jsonResult = Optional.ofNullable(redisService.get(code))
                .orElseThrow(() -> new UnauthenticatedException("???????????? ?????? code: " + code)).toString();

        Tokens tokens;
        try {
            tokens = objectMapper.readValue(jsonResult, Tokens.class);

            if (!tokens.isOneTimeCodeValid()) {
                throw new UnauthenticatedException("???????????? ?????? code: " + code);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        tokens.expireOnetimeCode();

        updateTokenToRedis(tokens);

        return tokens;
    }

    private Date getExpirationTime(Long validSeconds) {
        return new Date(new Date().getTime() + validSeconds * 1000);
    }

    public Map<String, Object> extractUserInfoFromToken(String accessToken) {
        return jwtService.extractUserInfoFromToken(accessToken);
    }
}
