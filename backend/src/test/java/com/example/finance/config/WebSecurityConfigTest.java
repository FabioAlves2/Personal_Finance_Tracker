package com.example.finance.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.finance.controller.AuthController;
import com.example.finance.controller.TransactionController;
import com.example.finance.security.CustomUserDetailService;
import com.example.finance.security.JwtAuthenticationFilter;
import com.example.finance.security.JwtDecoder;
import com.example.finance.security.JwtToPrincipalConverter;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.AuthService;
import com.example.finance.service.TransactionService;
import com.example.finance.service.UserService;

/**
 * Exercises the real {@link SecurityFilterChain} bean end to end (via MockMvc)
 * rather than mocking it, so a regression like scoping it to "/api/**" only
 * (which silently left every other path unauthenticated) shows up as a failing test.
 */
@WebMvcTest(controllers = { AuthController.class, TransactionController.class })
@Import({ WebSecurityConfig.class, JwtAuthenticationFilter.class })
class WebSecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthService authService;
    @MockBean private UserService userService;
    @MockBean private TransactionService transactionService;
    @MockBean private CustomUserDetailService customUserDetailService;
    @MockBean private JwtDecoder jwtDecoder;
    @MockBean private JwtToPrincipalConverter jwtToPrincipalConverter;

    @Test
    void unauthenticatedRequestToProtectedEndpointIsRejected() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authEndpointsArePubliclyReachableWithoutAToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void validBearerTokenGrantsAccessToProtectedEndpoint() throws Exception {
        DecodedJWT decoded = mock(DecodedJWT.class);
        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L)
                .email("jane@example.com")
                .name("Jane")
                .authorities(List.of())
                .build();
        when(jwtDecoder.decode("good-token")).thenReturn(decoded);
        when(jwtToPrincipalConverter.convert(decoded)).thenReturn(principal);
        when(transactionService.search(any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk());
    }
}
