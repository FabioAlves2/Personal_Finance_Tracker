package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.finance.model.User;
import com.example.finance.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;

    private UserService userService() {
        return new UserService(users, encoder);
    }

    @Test
    void createHashesPasswordWithoutTrimmingSoItMatchesWhatLoginSends() {
        UserService service = userService();
        when(users.existsByEmail("jane@example.com")).thenReturn(false);
        when(encoder.encode(" secret ")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        service.create("Jane Doe", "jane@example.com", " secret ");

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(encoder).encode(passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo(" secret ");
    }

    @Test
    void createRejectsDuplicateEmail() {
        UserService service = userService();
        when(users.existsByEmail(eq("jane@example.com"))).thenReturn(true);

        assertThatExceptionOfType(DuplicateKeyException.class)
                .isThrownBy(() -> service.create("Jane Doe", "jane@example.com", "secret"));
    }

    @Test
    void changePasswordFailsWhenCurrentPasswordWrong() {
        UserService service = userService();
        User user = User.builder().id(1L).name("Jane").email("jane@example.com")
                .hashedPassword("hashed").role("ROLE_USER").build();
        when(users.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(encoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.changePassword(1L, "wrong", "newPass"));
    }
}
