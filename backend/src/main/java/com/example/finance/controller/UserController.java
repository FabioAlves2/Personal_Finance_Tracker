package com.example.finance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.dto.ChangePasswordRequest;
import com.example.finance.dto.UpdateProfileRequest;
import com.example.finance.dto.UserDto;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing the authenticated user's profile")
public class UserController {

    private final UserService userService;
    
    @Operation(
        summary = "Get current user info",
        description = "Returns a greeting with the authenticated user's email and ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user info"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public String me(@AuthenticationPrincipal UserPrincipal principal) {
        return String.format("Logged in as: %s (ID: %d)", principal.getEmail(), principal.getUserId());
    }

    @Operation(
        summary = "Update user profile",
        description = "Updates the name of the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input - name cannot be blank"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/update-profile")
    public UserDto updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Request containing the new name", required = true)
            @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(principal.getUserId(), req.name());
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(principal.getUserId(), req.currentPassword(), req.newPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
}