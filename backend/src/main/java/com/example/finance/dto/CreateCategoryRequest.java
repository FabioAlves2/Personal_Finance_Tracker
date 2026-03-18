package com.example.finance.dto;

import com.example.finance.model.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
    @Schema(description = "Category name", example = "GROCERIES")
    @NotBlank String name,

    @Schema(description = "Category kind", example = "EXPENSE")
    @NotNull Category.Kind kind,

    @Schema(description = "Category color", example = "#EF4444")
    String color,
    
    @Schema(description = "Category icon", example = "shopping-cart")
    String icon) {
}
