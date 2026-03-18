package com.example.finance.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;

    public UserToken(UserPrincipal principal) {
        super(principal.getAuthorities());
        this.principal=principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }
    
}
