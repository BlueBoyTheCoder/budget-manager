package com.example.budgetmanager.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindCategoryByNameRegardlessOfCase() {
        // Given
        Category category = new Category(null, "HealthAndBeauty", null);
        categoryRepository.save(category);

        // When
        Optional<Category> foundLower = categoryRepository.findByNameIgnoreCase("healthandbeauty");
        Optional<Category> foundUpper = categoryRepository.findByNameIgnoreCase("HEALTHANDBEAUTY");

        // Then
        assertThat(foundLower).isPresent();
        assertThat(foundLower.get().getName()).isEqualTo("HealthAndBeauty");

        assertThat(foundUpper).isPresent();
        assertThat(foundUpper.get().getName()).isEqualTo("HealthAndBeauty");
    }

    @Test
    void shouldReturnEmptyOptionalWhenCategoryDoesNotExist() {
        // When
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("NonExistingCategory");

        // Then
        assertThat(found).isEmpty();
    }
}