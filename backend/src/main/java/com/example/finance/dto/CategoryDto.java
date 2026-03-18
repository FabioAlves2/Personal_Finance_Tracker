package com.example.finance.dto;

import com.example.finance.model.Category;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryDto(
  @Schema(description = "Category ID", example = "1")
  Long id,
  

  @Schema(description = "Category name", example = "GROCERIES")
  String name,

  @Schema(description = "Category kind", example = "EXPENSE")
  Category.Kind kind,

  @Schema(description = "Category color", example = "#EF4444")
  String color,

  @Schema(description = "Category icon", example = "shopping-cart")
  String icon, boolean isDefault
) {
  public static CategoryDto from(Category c) {
    return new CategoryDto(c.getId(), c.getName(), c.getKind(), c.getColor(), c.getIcon(), c.getOwner()==null);
  }
}
