package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.finance.dto.CreateTransactionRequest;
import com.example.finance.dto.TransactionDto;
import com.example.finance.dto.UpdateTransactionRequest;
import com.example.finance.model.Category;
import com.example.finance.model.Transaction;
import com.example.finance.model.User;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactions;
    @Mock private UserRepository users;
    @Mock private CategoryRepository categories;

    private TransactionService service() {
        return new TransactionService(transactions, users, categories);
    }

    private Category categoryWithKind(Category.Kind kind) {
        return Category.builder().id(2L).name("Savings").kind(kind).build();
    }

    @Test
    void savingCategoryProducesSavingTransactionInsteadOfExpense() {
        TransactionService service = service();
        User user = User.builder().id(1L).build();
        Category savingsCategory = categoryWithKind(Category.Kind.SAVING);

        when(users.getReferenceById(1L)).thenReturn(user);
        when(categories.findByIdAndOwnerId(2L, 1L)).thenReturn(java.util.Optional.of(savingsCategory));
        when(transactions.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new CreateTransactionRequest(BigDecimal.TEN, LocalDate.now(), "to savings", 2L);
        TransactionDto dto = service.saveTransaction(1L, req);

        assertThat(dto.type()).isEqualTo(Transaction.Type.SAVING);
    }

    @Test
    void incomeCategoryProducesIncomeTransaction() {
        TransactionService service = service();
        User user = User.builder().id(1L).build();
        Category incomeCategory = categoryWithKind(Category.Kind.INCOME);

        when(users.getReferenceById(1L)).thenReturn(user);
        when(categories.findByIdAndOwnerId(2L, 1L)).thenReturn(java.util.Optional.of(incomeCategory));
        when(transactions.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new CreateTransactionRequest(BigDecimal.TEN, LocalDate.now(), "salary", 2L);
        TransactionDto dto = service.saveTransaction(1L, req);

        assertThat(dto.type()).isEqualTo(Transaction.Type.INCOME);
    }

    @Test
    void searchDelegatesToRepositoryViaSpecification() {
        TransactionService service = service();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(transactions.findAll(any(Specification.class), any(Sort.class))).thenReturn(java.util.List.of());

        service.search(1L, Transaction.Type.EXPENSE, 2L, start, end);

        org.mockito.Mockito.verify(transactions).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "date")));
    }

    @Test
    void updateNotFoundThrows() {
        TransactionService service = service();
        when(transactions.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        var req = new UpdateTransactionRequest(BigDecimal.ONE, null, null, null);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.update(1L, 99L, req));
    }

    @Test
    void updateWithNewCategoryRecomputesTypeFromNewCategoryKind() {
        TransactionService service = service();
        Category oldCategory = categoryWithKind(Category.Kind.EXPENSE);
        Transaction existing = Transaction.builder()
                .id(10L)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .category(oldCategory)
                .type(Transaction.Type.EXPENSE)
                .build();
        Category savingsCategory = Category.builder().id(3L).name("Savings").kind(Category.Kind.SAVING).build();

        when(transactions.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(categories.findByIdAndOwnerId(3L, 1L)).thenReturn(Optional.of(savingsCategory));

        var req = new UpdateTransactionRequest(null, null, null, 3L);
        TransactionDto dto = service.update(1L, 10L, req);

        assertThat(dto.type()).isEqualTo(Transaction.Type.SAVING);
        assertThat(dto.categoryId()).isEqualTo(3L);
    }

    @Test
    void updateWithOnlyAmountLeavesCategoryAndTypeUnchanged() {
        TransactionService service = service();
        Category category = categoryWithKind(Category.Kind.EXPENSE);
        Transaction existing = Transaction.builder()
                .id(10L)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("old")
                .category(category)
                .type(Transaction.Type.EXPENSE)
                .build();

        when(transactions.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        var req = new UpdateTransactionRequest(new BigDecimal("50.00"), null, null, null);
        TransactionDto dto = service.update(1L, 10L, req);

        assertThat(dto.amount()).isEqualByComparingTo("50.00");
        assertThat(dto.type()).isEqualTo(Transaction.Type.EXPENSE);
        assertThat(dto.categoryId()).isEqualTo(2L);
        assertThat(dto.description()).isEqualTo("old");
        org.mockito.Mockito.verify(categories, org.mockito.Mockito.never()).findByIdAndOwnerId(any(), any());
    }
}
