package com.example.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
    
    @Schema(description = "User name", example = "João Silva")
    String name,
    
    @Schema(description = "User email", example = "joao@example.com")
    String email,
    
    @Schema(description = "User password", example = "password")
    String password) {
    
}
