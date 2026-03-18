package com.example.finance.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtToPrincipalConverter {
    public UserPrincipal convert(DecodedJWT jwt){
        return UserPrincipal.builder()
                .userId(Long.valueOf(jwt.getSubject()))
                .email(jwt.getClaim("e").asString())
                .authorities(extractAuthorities(jwt))
                .build();

    }

    private List<SimpleGrantedAuthority> extractAuthorities(DecodedJWT jwt){
        var claim = jwt.getClaim("a");
        if (claim.isNull() || claim.isMissing()) {
            return List.of();
        }
        var roles = claim.asList(String.class);
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }
    
}
