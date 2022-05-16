package com.example.user.service;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.JsonException;
import com.example.user.exception.UnauthenticatedException;
import com.example.user.security.AbstractJwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class TokenService {
    public static final Long ACCESSTOKEN_VALID_MINUTES = 30L;
    public static final Long ACCESSTOKEN_VALID_SECONDS = ACCESSTOKEN_VALID_MINUTES * 60L;
    public static final Long REFRESHTOKEN_VALID_MINUTES = 24 * 60L;
    public static final Long REFRESHTOKEN_VALID_SECONDS = REFRESHTOKEN_VALID_MINUTES * 60L;
    public static final Long ONETIMECODE_VALID_SECONDS = 5 * 60L;

    private final AbstractJwtService jwtService;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Tokens generateTokens(UserProfiles userProfiles, boolean isOAuth2) {
        String jwtId = UUID.randomUUID().toString();

        Tokens tokens = Tokens.builder()
                .jwtId(jwtId)
                .accessToken(generateAccessToken(jwtId, createUserProfilesClaim(userProfiles)))
                .refreshToken(generateRefreshToken())
                .isOneTimeCodeValid(isOAuth2)
                .build();

        updateTokenToRedis(jwtId, tokens, isOAuth2 ? ONETIMECODE_VALID_SECONDS : REFRESHTOKEN_VALID_SECONDS);

        return tokens;
    }

    private void setRedisTimeout(String jwtId, long timeoutSeconds) {
        redisTemplate.expire(jwtId, timeoutSeconds, TimeUnit.SECONDS);
    }

    private void removeRedis(String jwtId) {
        redisTemplate.delete(jwtId);
    }


    private Map<String, Object> createUserProfilesClaim(UserProfiles userProfiles) {
        Map<String, Object> userProfilesClaim = new HashMap<>();
        userProfilesClaim.put("userId", userProfiles.getUserId());
        userProfilesClaim.put("role", userProfiles.getRole());

        return userProfilesClaim;
    }

    private void updateTokenToRedis(String jwtId, Tokens tokens, long timeoutSeconds){
        try {
            redisTemplate.opsForValue().set(jwtId, objectMapper.writeValueAsString(tokens),
                    timeoutSeconds, TimeUnit.SECONDS);

        } catch (JsonProcessingException e) {
            throw new JwtException("JWT 토큰 생성시 오류가 발생했습니다. " + e);
        }
    }

    public String generateAccessToken(String jwtId, Map<String, Object> userProfilesClaim) {
        return jwtService.generateToken(jwtId, userProfilesClaim, getExpirationTime(ACCESSTOKEN_VALID_SECONDS));
    }

    public String generateRefreshToken() {
        return jwtService.generateToken(null,null, getExpirationTime(ACCESSTOKEN_VALID_SECONDS));
    }

    public Tokens getTokens(String code) {
        String jsonResult = Optional.ofNullable(redisTemplate.opsForValue().get(code))
                .orElseThrow(() -> new UnauthenticatedException("유효하지 않은 code: " + code)).toString();

        Tokens tokens;
        try {
            tokens = objectMapper.readValue(jsonResult, Tokens.class);

            if (!tokens.isOneTimeCodeValid()) {
                throw new UnauthenticatedException("유효하지 않은 code: " + code);
            }
        } catch (JsonProcessingException e) {
            throw new JsonException("Json 파싱 중에 오류가 발생했습니다. " + e);
        }

        tokens.expireOnetimeCode();

        updateTokenToRedis(code, tokens, REFRESHTOKEN_VALID_SECONDS);

        return tokens;
    }

    private Date getExpirationTime(Long validSeconds) {
        return new Date(new Date().getTime() + validSeconds * 1000);
    }

    public Map<String, Object> extractUserInfoFromToken(String accessToken) {
        return jwtService.extractUserInfoFromToken(accessToken);
    }
}
