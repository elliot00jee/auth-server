package com.example.user.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
public class Tokens {
    private String jwtId;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private boolean isOneTimeCodeValid;

    public void expireOnetimeCode() {
        this.isOneTimeCodeValid = false;
    }
}
