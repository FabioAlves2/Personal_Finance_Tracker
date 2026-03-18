package com.example.finance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.dto.CreateTransactionRequest;
import com.example.finance.dto.TransactionDto;
import com.example.finance.dto.UpdateTransactionRequest;
import com.example.finance.model.Transaction;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.TransactionService;

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
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing user transactions")
public class TransactionController {
    
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns transactions for the authenticated user, optionally filtered by type, date range, or category.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of transactions",
                content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public List<TransactionDto> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Filter by transaction type (INCOME or EXPENSE)")
            @RequestParam(required = false) Transaction.Type type,
            @Parameter(description = "Start date for range filter (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date for range filter (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId
    ) {
        Long userId = principal.getUserId();

        if (type != null && start != null && end != null && categoryId == null) {
            return transactionService.getByUserIdAndTypeAndDate(userId, type, start, end);
        }
        if (type != null && start == null && end == null && categoryId == null) {
            return transactionService.getByUserAndType(userId, type);
        }
        if (categoryId != null && type == null && start == null && end == null) {
            return transactionService.getByUserIdAndCategoryId(userId, categoryId);
        }

        return transactionService.getTransactionsByUser(userId);
    }

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates a new transaction for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction created",
                content = @Content(schema = @Schema(implementation = TransactionDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public TransactionDto create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTransactionRequest req) {
        return transactionService.saveTransaction(principal.getUserId(), req);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates an existing transaction (partial update allowed)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated",
                content = @Content(schema = @Schema(implementation = TransactionDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public TransactionDto update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID of the transaction to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest req) {
        return transactionService.update(principal.getUserId(), id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Deletes a transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction deleted",
                content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public String delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID of the transaction to delete", example = "1")
            @PathVariable Long id) {
        transactionService.delete(principal.getUserId(), id);
        return "Transaction deleted!";
    }
}