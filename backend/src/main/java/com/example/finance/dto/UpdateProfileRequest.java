package com.example.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Schema(description = "New name for the user", example = "João Silva")
    @NotBlank @Size(max=100) String name) {
}
