package com.example.finance.dto;

import com.example.finance.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User data transfer object")
public record UserDto(
    @Schema(description = "User ID", example = "1")
    Long id,
    
    @Schema(description = "User full name", example = "João Silva")
    String name,
    
    @Schema(description = "User email", example = "joao@email.com")
    String email,
    
    @Schema(description = "User role", example = "ROLE_USER")
    String role
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}