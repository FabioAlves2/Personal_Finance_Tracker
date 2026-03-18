package com.example.finance.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.dto.CategoryDto;
import com.example.finance.dto.CreateCategoryRequest;
import com.example.finance.model.Category;
import com.example.finance.model.User;
import com.example.finance.repository.CategoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categories;
    private final EntityManager em;

    public List<Category> listDefaults(){
        return categories.findAllByOwnerIdIsNull();
    }

    public List<Category> listForUser(Long userId){
        return categories.findAllByOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listAvailableForUser(Long userId){
        List<Category> result = new ArrayList<>();
        result.addAll(listDefaults());
        result.addAll(listForUser(userId));
        return result.stream()
                .map(CategoryDto::from)
                .toList();
    }

    public Category createForUser(Long userId, String name, Category.Kind kind, String color, String icon){
        String norm = name.trim();
        if (categories.existsByOwnerIdAndNameIgnoreCase(userId, norm)) {
            throw new DuplicateKeyException("Category with that name already exists");
        }

        User user = em.getReference(User.class, userId);

        Category c = Category.builder()
            .owner(user)
            .name(norm)
            .kind(kind)
            .color(color)
            .icon(icon)
            .build();
        return categories.save(c);
    }

    @Transactional
    public Category update(Long userId, Long categoryId, CreateCategoryRequest req) {
        Category c = categories.findByIdAndOwnerId(categoryId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String newName = req.name().trim();

        // Verifica duplicado apenas se o nome for diferente do atual
        if (!c.getName().equalsIgnoreCase(newName)) {
            if (categories.existsByOwnerIdAndNameIgnoreCase(userId, newName)) {
                throw new DuplicateKeyException("Category with that name already exists");
            }
            c.setName(newName);
        }

        // Atualiza outros campos (podem ser alterados mesmo com o mesmo nome)
        if (req.kind() != null) {
            c.setKind(req.kind());
        }
        if (req.color() != null) {
            c.setColor(req.color());
        }
        if (req.icon() != null) {
            c.setIcon(req.icon());
        }

        return c;
    }

    @Transactional
    public void delete(Long userId, Long categoryId){
        Category c = categories.findByIdAndOwnerId(categoryId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categories.delete(c);
    }

}
