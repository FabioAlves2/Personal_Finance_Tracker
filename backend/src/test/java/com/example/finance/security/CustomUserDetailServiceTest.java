package com.example.finance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.finance.model.User;
import com.example.finance.service.UserService;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock private UserService userService;

    @Test
    void unknownEmailThrowsUsernameNotFoundException() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        var service = new CustomUserDetailService(userService);

        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> service.loadUserByUsername("missing@example.com"));
    }

    @Test
    void knownEmailBuildsPrincipalWithNameAndAuthorities() {
        User user = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .hashedPassword("hashed")
                .role("ROLE_USER")
                .build();
        when(userService.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        var service = new CustomUserDetailService(userService);

        UserPrincipal principal = (UserPrincipal) service.loadUserByUsername("jane@example.com");

        assertThat(principal.getUserId()).isEqualTo(1L);
        assertThat(principal.getName()).isEqualTo("Jane Doe");
        assertThat(principal.getPassword()).isEqualTo("hashed");
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }
}
