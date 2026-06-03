package com.example.budgetmanager.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

        private Long id;

        @NotBlank(message = "Account name cannot be empty")
        private String name;

        @NotNull(message = "Initial balance is required")
        private BigDecimal balance;
}