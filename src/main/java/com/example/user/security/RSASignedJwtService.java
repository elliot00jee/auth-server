package com.example.user.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class RSASignedJwtService extends AbstractJwtService {
    private static final String PRIVATE_KEY_PATH = "keys/private.der";
    private static final String PUBLIC_KEY_PATH ="keys/public.der";

    @Override
    public JWSHeader getJWSHeader() {
        return new JWSHeader.Builder(JWSAlgorithm.RS256).build();
    }

    @Override
    public JWSSigner getJWSSigner() {
        return new RSASSASigner(getPrivateKey());
    }

    @Override
    public JWSVerifier getJWSVerifier() {
        return new RSASSAVerifier((RSAPublicKey) getPublicKey());
    }

    private static PrivateKey getPrivateKey() {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_PATH));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(spec);

        } catch(Exception e) {
            throw new JwtException("PrivateKey 로딩에 실패했습니다. : " + e.getMessage());
        }
    }

    public static PublicKey getPublicKey() {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_PATH));

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(spec);

        } catch(Exception e) {
            throw new JwtException("PublicKey 로딩에 실패했습니다. : " + e.getMessage());
        }
    }
}
