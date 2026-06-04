package com.example.budgetmanager.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CategoryDto(
        Long id,

        @NotBlank(message = "Category name cannot be empty")
        String name,

        BigDecimal budgetLimit
) {}