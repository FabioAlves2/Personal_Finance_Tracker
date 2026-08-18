package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.example.finance.dto.CreateCategoryRequest;
import com.example.finance.model.Category;
import com.example.finance.model.User;
import com.example.finance.repository.CategoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categories;
    @Mock private EntityManager em;

    private CategoryService service() {
        return new CategoryService(categories, em);
    }

    @Test
    void createForUserRejectsDuplicateName() {
        CategoryService service = service();
        when(categories.existsByOwnerIdAndNameIgnoreCase(1L, "Groceries")).thenReturn(true);

        assertThatExceptionOfType(DuplicateKeyException.class)
                .isThrownBy(() -> service.createForUser(1L, "Groceries", Category.Kind.EXPENSE, "#fff", "cart"));
    }

    @Test
    void createForUserSavesTrimmedName() {
        CategoryService service = service();
        User ref = User.builder().id(1L).build();
        when(categories.existsByOwnerIdAndNameIgnoreCase(1L, "Groceries")).thenReturn(false);
        when(em.getReference(User.class, 1L)).thenReturn(ref);
        when(categories.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category saved = service.createForUser(1L, "  Groceries  ", Category.Kind.EXPENSE, "#fff", "cart");

        assertThat(saved.getName()).isEqualTo("Groceries");
        assertThat(saved.getOwner()).isEqualTo(ref);
        assertThat(saved.getKind()).isEqualTo(Category.Kind.EXPENSE);
    }

    @Test
    void updateNotFoundThrows() {
        CategoryService service = service();
        when(categories.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.empty());

        var req = new CreateCategoryRequest("Food", Category.Kind.EXPENSE, null, null);
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.update(1L, 5L, req));
    }

    @Test
    void updateRenamingToSameNameSkipsDuplicateCheckButStillUpdatesOtherFields() {
        CategoryService service = service();
        Category existing = Category.builder().id(5L).name("Groceries").kind(Category.Kind.EXPENSE).build();
        when(categories.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));

        var req = new CreateCategoryRequest("groceries", Category.Kind.SAVING, "#000", "icon");
        Category updated = service.update(1L, 5L, req);

        assertThat(updated.getName()).isEqualTo("Groceries");
        assertThat(updated.getKind()).isEqualTo(Category.Kind.SAVING);
        assertThat(updated.getColor()).isEqualTo("#000");
        assertThat(updated.getIcon()).isEqualTo("icon");
        verify(categories, never()).existsByOwnerIdAndNameIgnoreCase(any(), any());
    }

    @Test
    void updateRenamingToADifferentDuplicateNameThrows() {
        CategoryService service = service();
        Category existing = Category.builder().id(5L).name("Groceries").kind(Category.Kind.EXPENSE).build();
        when(categories.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));
        when(categories.existsByOwnerIdAndNameIgnoreCase(1L, "Food")).thenReturn(true);

        var req = new CreateCategoryRequest("Food", Category.Kind.EXPENSE, null, null);
        assertThatExceptionOfType(DuplicateKeyException.class)
                .isThrownBy(() -> service.update(1L, 5L, req));
    }

    @Test
    void deleteNotFoundThrows() {
        CategoryService service = service();
        when(categories.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.delete(1L, 5L));
    }

    @Test
    void deleteRemovesOwnedCategory() {
        CategoryService service = service();
        Category existing = Category.builder().id(5L).name("Groceries").build();
        when(categories.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));

        service.delete(1L, 5L);

        verify(categories).delete(existing);
    }
}
