package com.example.budgetmanager.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getBudgetLimit()))
                .toList();
    }

    public CategoryDto create(CategoryDto dto) {
        if (categoryRepository.findByNameIgnoreCase(dto.name()).isPresent()) {
            throw new IllegalStateException("Category with name '" + dto.name() + "' already exists");
        }

        Category category = new Category(null, dto.name(), dto.budgetLimit());
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName(), saved.getBudgetLimit());
    }
}