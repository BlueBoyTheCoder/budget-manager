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
            CategoryDto food = categoryService.create(new CategoryDto(null, "Food", new BigDecimal("600.00")));
            CategoryDto transport = categoryService.create(new CategoryDto(null, "Transport", null));
            CategoryDto salary = categoryService.create(new CategoryDto(null, "Salary", null));
            CategoryDto entertainment = categoryService.create(new CategoryDto(null, "Entertainment", new BigDecimal("300.00")));
            CategoryDto utilities = categoryService.create(new CategoryDto(null, "Utilities", null));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("4500.00"), TransactionType.INCOME, salary.id(),
                    "Monthly Salary", LocalDate.now().minusDays(5), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("1500.00"), TransactionType.INCOME, salary.id(),
                    "Quarterly Bonus", LocalDate.now().minusDays(2), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("120.00"), TransactionType.EXPENSE, food.id(),
                    "Weekly grocery shopping", LocalDate.now().minusDays(4), mainAccount.getId(), null
            ));
            transactionService.create(new TransactionDto(
                    null, new BigDecimal("45.50"), TransactionType.EXPENSE, food.id(),
                    "UberEats dinner", LocalDate.now().minusDays(1), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("250.00"), TransactionType.EXPENSE, transport.id(),
                    "Monthly public transport pass", LocalDate.now().minusDays(4), mainAccount.getId(), null
            ));
            transactionService.create(new TransactionDto(
                    null, new BigDecimal("80.00"), TransactionType.EXPENSE, transport.id(),
                    "Fuel", LocalDate.now(), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("350.00"), TransactionType.EXPENSE, utilities.id(),
                    "Electricity bill", LocalDate.now().minusDays(3), mainAccount.getId(), null
            ));
            transactionService.create(new TransactionDto(
                    null, new BigDecimal("60.00"), TransactionType.EXPENSE, utilities.id(),
                    "Internet subscription", LocalDate.now().minusDays(3), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("55.00"), TransactionType.EXPENSE, entertainment.id(),
                    "Netflix subscription", LocalDate.now().minusDays(2), mainAccount.getId(), null
            ));
            transactionService.create(new TransactionDto(
                    null, new BigDecimal("260.00"), TransactionType.EXPENSE, entertainment.id(),
                    "Concert ticket", LocalDate.now(), mainAccount.getId(), null
            ));

            transactionService.create(new TransactionDto(
                    null, new BigDecimal("1000.00"), TransactionType.INCOME, salary.id(),
                    "Transfer from regular savings", LocalDate.now().minusDays(1), savingsAccount.getId(), null
            ));

            System.out.println(">> Database successfully seeded!");
        }
    }
}