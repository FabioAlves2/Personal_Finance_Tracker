package com.example.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.finance.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

    List<Category> findAllByOwnerIdIsNull();
    List<Category> findAllByOwnerId(Long ownerId);
    Optional<Category> findByIdAndOwnerIdIsNull(Long id); // global
    Optional<Category> findByIdAndOwnerId(Long id, Long ownerId); // user-owned
    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}

