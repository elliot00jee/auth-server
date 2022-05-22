package com.example.user.service;

import com.example.user.entity.Tokens;
import com.example.user.entity.UserProfiles;
import com.example.user.security.AbstractJwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

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

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @Test
    void generateTokens_needOneTimeCode_true_verify_Tokens() throws JsonProcessingException {
        UserProfiles userProfiles = UserProfiles.builder()
                .username("Elliot Jee")
                .department("Software engineering")
                .userId("elliot.jee")
                .role("Admin")
                .build();

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

//        verify(redisService, times(1))`
//                .update(any(), eq(objectMapper.writeValueAsString(tokens)), eq(5 * 60L));
    }



}