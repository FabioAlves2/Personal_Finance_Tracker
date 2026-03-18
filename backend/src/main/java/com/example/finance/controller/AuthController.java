package com.example.finance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.dto.AuthRequest;
import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.RegisterRequest;
import com.example.finance.model.User;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.AuthService;
import com.example.finance.service.UserService;

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

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {
    
    private final AuthService auth;
    private final UserService users;
    
    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with email and password, returning a JWT token and user details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody AuthRequest req) {
        var res = auth.login(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account and returns an authentication token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterRequest req) {
        var res = auth.signIn(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generates a new JWT token for an already authenticated user. Requires a valid token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public AuthResponse refresh(
            @Parameter(hidden = true) // Injected automatically, no need to be displayed
            @AuthenticationPrincipal UserPrincipal principal) {
        String newToken = auth.refresh(principal);
        var name = users.findByEmail(principal.getEmail())
                .map(User::getName)
                .orElse(principal.getEmail());
        return new AuthResponse(newToken, principal.getUserId(), name);
    }
}