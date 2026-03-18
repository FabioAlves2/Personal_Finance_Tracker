package com.example.finance.security;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.finance.config.JwtProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtDecoder {
    private final JwtProperties properties;
    
    public DecodedJWT decode(String token){
        var secret = properties.getSecretKey();
        if (secret == null || secret.isBlank()) throw new IllegalStateException("JWT secret not configured");

        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }
}
