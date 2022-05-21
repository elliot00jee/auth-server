package com.example.user.security;

import com.example.user.exception.TokenException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractJwtService {

    public String generateToken(String jwtId, Map<String, Object> user, Date expirationTime) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(jwtId)
                    .claim("userInfo", user)
                    .issuer("elliot")
                    .issueTime(new Date())
                    .expirationTime(expirationTime)
                    .build();

            SignedJWT signedJWT = new SignedJWT(getJWSHeader(), claimsSet);
            signedJWT.sign(getJWSSigner());

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new JwtException("JWT 토큰 생성에 실패했습니다. " + e);
        }
    }

    public Map<String, Object> extractUserInfoFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (signedJWT.verify(getJWSVerifier())) {
                JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
                if (jwtClaimsSet != null && isTokenExpired(jwtClaimsSet)) {
                    throw new TokenException("만료된 토큰입니다.");
                }
                return (Map<String, Object>) jwtClaimsSet.getClaim("userInfo");
            } else {
                throw new TokenException("유효하지 않은 토큰입니다.");
            }
        } catch (ParseException | JOSEException e) {
            throw new TokenException("토큰 복호화 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    public boolean isTokenExpired(JWTClaimsSet claims) {
        return claims.getExpirationTime().before(new Date());
    }

    abstract JWSHeader getJWSHeader();

    abstract JWSSigner getJWSSigner();

    abstract JWSVerifier getJWSVerifier();
}
