package com.example.finance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.finance.config.JwtProperties;

class JwtDecoderTest {

    private final JwtProperties properties = new JwtProperties();
    private final JwtDecoder decoder;

    JwtDecoderTest() {
        properties.setSecretKey("unit-test-secret");
        decoder = new JwtDecoder(properties);
    }

    @Test
    void decodesTokenSignedWithTheSameSecret() {
        String token = JWT.create()
                .withSubject("42")
                .withExpiresAt(Instant.now().plusSeconds(60))
                .withClaim("e", "jane@example.com")
                .sign(Algorithm.HMAC256("unit-test-secret"));

        DecodedJWT decoded = decoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(decoded.getClaim("e").asString()).isEqualTo("jane@example.com");
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        String tampered = JWT.create()
                .withSubject("42")
                .withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256("a-different-secret"));

        assertThatExceptionOfType(JWTVerificationException.class)
                .isThrownBy(() -> decoder.decode(tampered));
    }

    @Test
    void rejectsExpiredToken() {
        String expired = JWT.create()
                .withSubject("42")
                .withExpiresAt(Instant.now().minusSeconds(5))
                .sign(Algorithm.HMAC256("unit-test-secret"));

        assertThatExceptionOfType(JWTVerificationException.class)
                .isThrownBy(() -> decoder.decode(expired));
    }

    @Test
    void rejectsMalformedToken() {
        assertThatExceptionOfType(JWTVerificationException.class)
                .isThrownBy(() -> decoder.decode("not-a-real-token"));
    }
}
