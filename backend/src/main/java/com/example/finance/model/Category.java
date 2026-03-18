package com.example.finance.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "categories",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_user_name", columnNames = {"user_id", "name"}),
    @UniqueConstraint(name = "uk_category_code", columnNames = {"code"})
  },
  indexes = {
    @Index(name = "idx_categories_user", columnList = "user_id")
  }
)

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor 
@Builder

public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 20)
    private String color;

    @Column(length = 20)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Kind kind = Kind.EXPENSE;

    public enum Kind { 
        EXPENSE, INCOME, SAVING
    }
}
