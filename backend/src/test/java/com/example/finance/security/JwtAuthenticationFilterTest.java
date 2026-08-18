package com.example.finance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtDecoder jwtDecoder;
    @Mock private JwtToPrincipalConverter jwtToPrincipalConverter;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtDecoder, jwtToPrincipalConverter);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeaderLeavesRequestUnauthenticated() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtDecoder);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void headerWithoutBearerPrefixIsIgnored() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtDecoder);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void validTokenAuthenticatesTheRequest() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        var response = new MockHttpServletResponse();

        DecodedJWT decoded = mock(DecodedJWT.class);
        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L)
                .email("jane@example.com")
                .name("Jane")
                .authorities(List.of())
                .build();
        when(jwtDecoder.decode("good-token")).thenReturn(decoded);
        when(jwtToPrincipalConverter.convert(decoded)).thenReturn(principal);

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(((UserPrincipal) authentication.getPrincipal()).getUserId()).isEqualTo(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void expiredOrTamperedTokenClearsContextButStillContinuesTheChain() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        var response = new MockHttpServletResponse();

        when(jwtDecoder.decode("bad-token")).thenThrow(new JWTVerificationException("token expired or tampered"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
