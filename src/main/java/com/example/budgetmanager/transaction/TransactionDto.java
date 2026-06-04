package com.example.budgetmanager.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
        private Long id;

        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Transaction amount must be greater than zero")
        private BigDecimal amount;

        @NotNull(message = "Transaction type (INCOME/EXPENSE) is required")
        private TransactionType type;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        private String description;

        @NotNull(message = "Transaction date is required")
        private LocalDate transactionDate;

        @NotNull(message = "Account ID is required")
        private Long accountId;

        private String warningMessage;
}