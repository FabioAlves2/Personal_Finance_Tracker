package com.example.finance.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.finance.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndType(Long userId, Transaction.Type type);
    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);
    List<Transaction> findByUserIdAndTypeAndDateBetween(Long userId, Transaction.Type type, LocalDate start, LocalDate end);
    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where t.user.id = :userId
            and t.type = :type
            and t.date between :start and :end
        """)
    BigDecimal sumByUserAndTypeInRange(Long userId, Transaction.Type type, LocalDate start, LocalDate end);


    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where t.user.id = :userId
            and t.category.id = :categoryId
            and t.type = :type
            and t.date between :start and :end
        """)
    BigDecimal totalByCategory(Long userId, Long categoryId, Transaction.Type type, LocalDate start, LocalDate end);
}
