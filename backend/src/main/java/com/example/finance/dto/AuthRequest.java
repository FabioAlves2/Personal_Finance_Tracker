package com.example.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequest(
    @Schema(description = "User email", example = "user@example.com")
    String email,
    
    @Schema(description = "User password", example = "password")
    String password
) {}
