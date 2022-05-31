package com.example.user.service;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.security.AbstractJwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("local")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    RedisService redisService;
    @Mock
    AbstractJwtService jwtService;
    static UserProfiles userProfiles;
    static ObjectMapper objectMapper;

    @BeforeAll
    static void createUserProfiles() {
        userProfiles = UserProfiles.builder()
                .username("Elliot Jee")
                .department("Software engineering")
                .userId("elliot.jee")
                .role("Admin")
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    class generateTokens {

        @Test
        void needOneTimeCode_true() throws JsonProcessingException {
            doNothing().when(redisService).update(anyString(), anyString(), anyLong());

            when(jwtService.generateToken(any(), any(), any())).thenReturn("accesstoken");
            when(jwtService.generateToken(eq(null), eq(null), any())).thenReturn("refreshtoken");

            TokenService tokenService = new TokenService(jwtService, redisService, objectMapper);

            Tokens tokens = tokenService.generateTokens(userProfiles, true);

            assertThat(tokens.getJwtId()).isNotNull();
            assertThat(tokens.getUserId()).isEqualTo(userProfiles.getUserId());
            assertThat(tokens.getAccessToken()).isEqualTo("accesstoken");
            assertThat(tokens.getRefreshToken()).isEqualTo("refreshtoken");
            assertThat(tokens.isOneTimeCodeValid()).isTrue();

            verify(redisService, times(1))
                .update(any(), eq(objectMapper.writeValueAsString(tokens)), eq(5 * 60L));
        }
        @Test
        void needOneTimeCode_false() throws JsonProcessingException {
            doNothing().when(redisService).update(anyString(), anyString(), anyLong());

            when(jwtService.generateToken(any(), any(), any())).thenReturn("accesstoken");
            when(jwtService.generateToken(eq(null), eq(null), any())).thenReturn("refreshtoken");

            TokenService tokenService = new TokenService(jwtService, redisService, objectMapper);

            Tokens tokens = tokenService.generateTokens(userProfiles, false);

            assertThat(tokens.getJwtId()).isNotNull();
            assertThat(tokens.getUserId()).isEqualTo(userProfiles.getUserId());
            assertThat(tokens.getAccessToken()).isEqualTo("accesstoken");
            assertThat(tokens.getRefreshToken()).isEqualTo("refreshtoken");
            assertThat(tokens.isOneTimeCodeValid()).isFalse();

            verify(redisService, times(1))
                    .update(any(), eq(objectMapper.writeValueAsString(tokens)), eq(24 * 60 * 60L));
        }
    }


}