package com.example.budgetmanager.summary;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDto {
        @NotNull(message = "Transaction amount is required")
        @PositiveOrZero(message = "Transaction amount must at least zero")
        private BigDecimal totalIncome;

        @NotNull(message = "Transaction amount is required")
        @PositiveOrZero(message = "Transaction amount must at least zero")
        private BigDecimal totalExpenses;

        private List<CategoryOverviewDto> expensesByCategory;
}