package com.example.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.dto.CreateTransactionRequest;
import com.example.finance.dto.TransactionDto;
import com.example.finance.dto.UpdateTransactionRequest;
import com.example.finance.model.Category;
import com.example.finance.model.Transaction;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactions;
    private final UserRepository users;
    private final CategoryRepository categories;
    
    @Transactional
    public TransactionDto saveTransaction(Long userId, CreateTransactionRequest req){
        var uRef = users.getReferenceById(userId);
        var category = getAccessibleCategory(userId, req.categoryId());


        var t = Transaction.builder()
            .amount(req.amount())
            .date(req.date())
            .description(req.description())
            .user(uRef)
            .category(category)
            .type(category.getKind() == Category.Kind.INCOME ? Transaction.Type.INCOME : Transaction.Type.EXPENSE)
            .build();

        Transaction saved = transactions.save(t);
        return TransactionDto.from(saved);
    }

    @Transactional
    public TransactionDto update(Long userId, Long transactionId, UpdateTransactionRequest req) {
        var t = transactions.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (req.categoryId() != null && (t.getCategory() == null || !t.getCategory().getId().equals(req.categoryId()))) {
            Category cat = getAccessibleCategory(userId, req.categoryId());
            t.setCategory(cat);
            t.setType(cat.getKind() == Category.Kind.INCOME ? Transaction.Type.INCOME : Transaction.Type.EXPENSE);
        }
        if (req.amount() != null)      t.setAmount(req.amount());
        if (req.date() != null)        t.setDate(req.date());
        if (req.description() != null) t.setDescription(req.description());

        return TransactionDto.from(t);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        var t = transactions.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transactions.delete(t);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByUser(Long userId) {
        return TransactionDto.fromList(transactions.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getByUserAndType(Long userId, Transaction.Type type) {
        return TransactionDto.fromList(transactions.findByUserIdAndType(userId, type));
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getByUserIdAndCategoryId(Long userId, Long categoryId) {
        return TransactionDto.fromList(transactions.findByUserIdAndCategoryId(userId, categoryId));
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getByUserIdAndTypeAndDate(Long userId, Transaction.Type type, LocalDate start, LocalDate end) {
        return TransactionDto.fromList(transactions.findByUserIdAndTypeAndDateBetween(userId, type, start, end));
    }

    @Transactional(readOnly = true)
    public BigDecimal totalByType(Long userId, Transaction.Type type, LocalDate start, LocalDate end) {
        return transactions.sumByUserAndTypeInRange(userId, type, start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalForCategory(Long userId, Long categoryId, Transaction.Type type, LocalDate start, LocalDate end) {
        return transactions.totalByCategory(userId, categoryId, type, start, end);
    }

    private Category getAccessibleCategory(Long userId, Long categoryId) {
        return categories.findByIdAndOwnerId(categoryId, userId)
            .or(() -> categories.findByIdAndOwnerIdIsNull(categoryId))
            .orElseThrow(() -> new IllegalArgumentException("Category not found or not accessible"));
    }
}