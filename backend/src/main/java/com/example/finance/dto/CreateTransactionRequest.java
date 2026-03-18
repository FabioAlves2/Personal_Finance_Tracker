package com.example.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTransactionRequest(
    @Schema(description = "Transaction amount", example = "1500.00")
    @NotNull(message = "Amount is required")
    BigDecimal amount,

    @Schema(description = "Transaction date", example = "2026-03-10")
    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date,

    @Schema(description = "Transaction description", example = "Monthly salary")
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    String description,

    @Schema(description = "Category ID", example = "2")
    @NotNull(message = "Category is required")
    Long categoryId
) {}