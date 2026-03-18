package com.example.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.example.finance.model.Category;
import com.example.finance.model.Transaction;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransactionDto(
    @Schema(description = "Transaction ID", example = "1")
    Long id,

    @Schema(description = "Transaction amount", example = "1500.00")
    BigDecimal amount,

    @Schema(description = "Transaction date", example = "2026-03-10")
    LocalDate date,

    @Schema(description = "Transaction type", example = "INCOME")
    Transaction.Type type,

    @Schema(description = "Transaction description", example = "Monthly salary")
    String description,

    @Schema(description = "Category ID", example = "2")
    Long categoryId,

    @Schema(description = "Category name", example = "Salary")
    String categoryName
) {
    public static TransactionDto from(Transaction t) {
        Category c = t.getCategory();
        Long catId = (c != null) ? c.getId() : null;
        String catName = (c != null) ? c.getName() : null;

        return new TransactionDto(
            t.getId(),
            t.getAmount(),
            t.getDate(),
            t.getType(),
            t.getDescription(),
            catId,
            catName
        );
    }

    public static List<TransactionDto> fromList(Collection<Transaction> txs) {
        return txs.stream().map(TransactionDto::from).toList();
    }
}