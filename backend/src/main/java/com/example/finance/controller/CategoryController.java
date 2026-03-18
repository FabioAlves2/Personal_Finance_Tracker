package com.example.finance.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.dto.CategoryDto;
import com.example.finance.dto.CreateCategoryRequest;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
@Tag(name = "Categories", description = "Endpoints for managing user categories")
public class CategoryController {
    
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List categories", description = "Returns all categories available to the authenticated user (global + user's own)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of categories", 
                content = @Content(schema = @Schema(implementation = CategoryDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public List<CategoryDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return categoryService.listAvailableForUser(principal.getUserId());
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Creates a new personal category for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category created", 
                content = @Content(schema = @Schema(implementation = CategoryDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate name", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public CategoryDto create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCategoryRequest req) {
        return CategoryDto.from(categoryService.createForUser(principal.getUserId(), req.name(), req.kind(), req.color(), req.icon()));
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "Update category", description = "Updates an own existing category (name, kind, color, icon)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated", 
                content = @Content(schema = @Schema(implementation = CategoryDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate name", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public CategoryDto update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID of the category to update", example = "1")
            @PathVariable Long categoryId,
            @Valid @RequestBody CreateCategoryRequest req) {
        return CategoryDto.from(categoryService.update(principal.getUserId(), categoryId, req));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category", description = "Deletes a user's own category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category deleted", 
                content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public String delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID of the category to delete", example = "1")
            @PathVariable Long categoryId) {
        categoryService.delete(principal.getUserId(), categoryId);
        return "Category deleted!";
    }
}