package com.example.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateTransactionRequest(
    @Schema(description = "Transaction amount (optional)", example = "1500.00")
    BigDecimal amount,

    @Schema(description = "Transaction date (optional)", example = "2026-03-10")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date,

    @Schema(description = "Transaction description (optional)", example = "Updated salary description")
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    String description,

    @Schema(description = "Category ID (optional)", example = "3")
    Long categoryId
) {}