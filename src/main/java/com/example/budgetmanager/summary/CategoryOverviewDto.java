package com.example.budgetmanager.summary;

import java.math.BigDecimal;

public record CategoryOverviewDto(
        String categoryName,
        BigDecimal totalExpenses
) {}