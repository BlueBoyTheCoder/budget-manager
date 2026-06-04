package com.example.budgetmanager.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    // --- TESTY DLA METODY getAll() ---

    @Test
    void shouldReturnListOfCategoryDtosWhenCategoriesExist() {
        // Given
        Category food = new Category(1L, "Food");
        Category rent = new Category(2L, "Rent");
        when(categoryRepository.findAll()).thenReturn(List.of(food, rent));

        // When
        List<CategoryDto> result = categoryService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Food");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Rent");

        // Weryfikujemy, czy metoda z repozytorium wykonała się dokładnie raz
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        // Given
        when(categoryRepository.findAll()).thenReturn(List.of());

        // When
        List<CategoryDto> result = categoryService.getAll();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findAll();
    }

    // --- TESTY DLA METODY create() ---

    @Test
    void shouldCreateCategorySuccessfullyWhenNameIsUnique() {
        // Given
        CategoryDto inputDto = new CategoryDto(null, "Transport");
        Category savedCategory = new Category(10L, "Transport");

        // Dokładne mockowanie wywołań wewnątrz metody create()
        when(categoryRepository.findByNameIgnoreCase("Transport")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryDto result = categoryService.create(inputDto);

        // Then
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Transport");

        // Weryfikujemy, że repozytorium sprawdziło unikalność i zapisało obiekt
        verify(categoryRepository, times(1)).findByNameIgnoreCase("Transport");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameAlreadyExists() {
        // Given
        CategoryDto inputDto = new CategoryDto(null, "FOOD");
        Category existingCategory = new Category(1L, "Food");

        // Mockujemy sytuację, w której baza znajduje już taką kategorię
        when(categoryRepository.findByNameIgnoreCase("FOOD")).thenReturn(Optional.of(existingCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.create(inputDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Category with name 'FOOD' already exists");

        // WAŻNE: Weryfikujemy, że metoda save NIGDY nie została wywołana (bo rzuciliśmy wyjątek wcześniej)
        verify(categoryRepository, times(1)).findByNameIgnoreCase("FOOD");
        verify(categoryRepository, never()).save(any(Category.class));
    }
}