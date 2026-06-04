package com.example.budgetmanager.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public List<CategoryDto> getAll() {
        return repository.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getBudgetLimit()))
                .toList();
    }

    public CategoryDto create(CategoryDto dto) {
        if (repository.findByNameIgnoreCase(dto.name()).isPresent()) {
            throw new IllegalStateException("Category with name '" + dto.name() + "' already exists");
        }

        Category category = new Category(null, dto.name(), dto.budgetLimit());
        Category saved = repository.save(category);
        return new CategoryDto(saved.getId(), saved.getName(), saved.getBudgetLimit());
    }
}