package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.jwt.JWT;
import com.example.finance.config.JwtProperties;
import com.example.finance.dto.AuthRequest;
import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.RegisterRequest;
import com.example.finance.dto.UserDto;
import com.example.finance.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService users;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("test-secret-key-for-unit-tests");
        authService = new AuthService(users, jwtProperties, authenticationManager);
    }

    private UserPrincipal principal() {
        return UserPrincipal.builder()
                .userId(1L)
                .email("jane@example.com")
                .name("Jane Doe")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .password("hashed")
                .build();
    }

    private void stubAuthenticationWith(UserPrincipal principal) {
        var authentication = new TestingAuthenticationToken(principal, null, "ROLE_USER");
        authentication.setAuthenticated(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
    }

    @Test
    void loginIssuesTokenThatExpiresAboutOneHourFromNow() {
        stubAuthenticationWith(principal());

        AuthResponse res = authService.login(new AuthRequest("jane@example.com", "secret"));

        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.userName()).isEqualTo("Jane Doe");

        var decoded = JWT.decode(res.accessToken());
        long secondsToExpiry = decoded.getExpiresAtAsInstant().getEpochSecond() - Instant.now().getEpochSecond();
        assertThat(secondsToExpiry).isGreaterThan(600);
    }

    @Test
    void loginUsesPrincipalNameWithoutAnyExtraLookup() {
        stubAuthenticationWith(principal());

        authService.login(new AuthRequest("jane@example.com", "secret"));

        verifyNoInteractions(users);
    }

    @Test
    void signInCreatesUserThenAuthenticates() {
        when(users.create("Jane Doe", "jane@example.com", "secret"))
                .thenReturn(new UserDto(1L, "Jane Doe", "jane@example.com", "ROLE_USER"));
        stubAuthenticationWith(principal());

        AuthResponse res = authService.signIn(new RegisterRequest("Jane Doe", "jane@example.com", "secret"));

        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.userName()).isEqualTo("Jane Doe");
    }
}
