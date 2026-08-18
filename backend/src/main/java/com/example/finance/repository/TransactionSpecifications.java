package com.example.finance.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.example.finance.model.Transaction;

import jakarta.persistence.criteria.JoinType;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> search(Long userId, Transaction.Type type, Long categoryId, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("category", JoinType.INNER);
            }

            var predicate = cb.equal(root.get("user").get("id"), userId);
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), categoryId));
            }
            if (start != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("date"), start));
            }
            if (end != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("date"), end));
            }
            return predicate;
        };
    }
}
