package com.example.finance.service;

import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.dto.UserDto;
import com.example.finance.model.User;
import com.example.finance.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    
    public User getById(Long id){
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Optional<User> findByEmail(String email){
        return users.findByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public UserDto create(String name, String email, String password){
        String newEmail = email.trim().toLowerCase();
        if (users.existsByEmail(newEmail)){
            throw new DuplicateKeyException("Email already exists, try login");
        }
        User u = User.builder()
            .name(name.trim())
            .email(newEmail)
            .hashedPassword(encoder.encode(password))
            .role("ROLE_USER")
            .build();
        User saved = users.save(u);
        return UserDto.from(saved);
    }

    @Transactional
    public UserDto updateProfile(Long userId, String name) {
        User u = getById(userId);
        u.setName(name.trim());
        return UserDto.from(u);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword){
        User u = getById(userId);
        if (encoder.matches(currentPassword, u.getHashedPassword())) {
            u.setHashedPassword(encoder.encode(newPassword));
        } else {
            throw new IllegalArgumentException("Current Password is wrong!");
        }
    }
}