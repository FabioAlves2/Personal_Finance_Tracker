package com.example.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
    @Schema(description = "User access token", example = "auto-generated token")
    String accessToken, 

    @Schema(description = "User ID", example = "1")
    Long userId,

    @Schema(description = "User name", example = "João Silva")
    String userName) {
}