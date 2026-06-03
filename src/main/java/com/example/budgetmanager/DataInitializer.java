package com.example.budgetmanager;

import com.example.budgetmanager.account.AccountDto;
import com.example.budgetmanager.account.AccountService;
import com.example.budgetmanager.category.CategoryDto;
import com.example.budgetmanager.category.CategoryService;
import com.example.budgetmanager.transaction.TransactionDto;
import com.example.budgetmanager.transaction.TransactionService;
import com.example.budgetmanager.transaction.TransactionType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public DataInitializer(AccountService accountService, CategoryService categoryService, TransactionService transactionService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (accountService.getAllAccounts().isEmpty()) {

            // Seed Accounts
            AccountDto mainAccount = accountService.createAccount(new AccountDto(null, "Main Account", new BigDecimal("5000.00")));
            AccountDto savingsAccount = accountService.createAccount(new AccountDto(null, "Savings Account", new BigDecimal("10000.00")));

            // Seed Categories
            CategoryDto food = categoryService.create(new CategoryDto(null, "Food"));
            CategoryDto transport = categoryService.create(new CategoryDto(null, "Transport"));
            CategoryDto salary = categoryService.create(new CategoryDto(null, "Salary"));

            // Seed Transactions
            transactionService.create(new TransactionDto(
                    null,
                    new BigDecimal("1500.00"),
                    TransactionType.INCOME,
                    salary.id(),
                    "Bonus",
                    LocalDate.now(),
                    mainAccount.getId()
            ));

            transactionService.create(new TransactionDto(
                    null,
                    new BigDecimal("120.00"),
                    TransactionType.EXPENSE,
                    food.id(),
                    "Weekly dinner",
                    LocalDate.now(),
                    mainAccount.getId()
            ));

            System.out.println(">> Database successfully seeded with demo data!");
        }
    }
}