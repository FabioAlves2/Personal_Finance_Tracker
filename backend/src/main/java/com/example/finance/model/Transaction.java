package com.example.finance.model;

import java.time.LocalDate;
import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(
  name = "transactions",
  indexes = {
    @Index(name = "idx_transactions_user", columnList = "user_id"),
    @Index(name = "idx_transactions_category", columnList = "category_id"),
    @Index(name = "idx_transactions_date", columnList = "transaction_date")
  }
)

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @PastOrPresent
    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(length = 150)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Type type = Type.EXPENSE;

    public enum Type{
        EXPENSE, INCOME, SAVING
    }
    
}