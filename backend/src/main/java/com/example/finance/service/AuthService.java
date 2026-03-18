package com.example.finance.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.finance.config.JwtProperties;
import com.example.finance.dto.AuthRequest;
import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.RegisterRequest;
import com.example.finance.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService users;
    private final JwtProperties jwt;
    private final AuthenticationManager authenticationManager;


    public AuthResponse login(AuthRequest req){
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var principal = (UserPrincipal) authentication.getPrincipal();

        var roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        var token = issue(principal.getEmail(), principal.getUserId(), roles);

        var name = users.findByEmail(principal.getEmail())
            .map(u -> u.getName())
            .orElse(principal.getEmail());
        return new AuthResponse(token, principal.getUserId(), name);
    }

    public AuthResponse signIn(RegisterRequest req){
        var u = users.create(req.name(), req.email(), req.password());
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var principal = (UserPrincipal) authentication.getPrincipal();

        var roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        var token = issue(principal.getEmail(), principal.getUserId(), roles);

        return new AuthResponse(token, principal.getUserId(), u.name());
    }

    private String issue(String email, Long userId, List<String> roles){
        var secret = jwt.getSecretKey();
        if (secret == null || secret.isBlank()) throw new IllegalStateException("JWT secret not configured");

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withExpiresAt(Instant.now().plusSeconds(60))
                .withIssuedAt(Instant.now())
                .withClaim("e", email)
                .withClaim("a", roles)
                .sign(Algorithm.HMAC256(secret));
    }

    public String refresh(UserPrincipal principal) {
        var roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return issue(principal.getEmail(), principal.getUserId(), roles);
    }
}
