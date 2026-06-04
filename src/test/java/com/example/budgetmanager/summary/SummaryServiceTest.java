package com.example.budgetmanager.summary;

import com.example.budgetmanager.account.Account;
import com.example.budgetmanager.account.AccountRepository;
import com.example.budgetmanager.category.Category;
import com.example.budgetmanager.category.CategoryRepository;
import com.example.budgetmanager.transaction.Transaction;
import com.example.budgetmanager.transaction.TransactionRepository;
import com.example.budgetmanager.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SummaryServiceTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private SummaryService summaryService;
    private Account sharedAccount;

    @BeforeEach
    void setUp() {
        summaryService = new SummaryService(transactionRepository);
        sharedAccount = accountRepository.save(new Account(null, "Test Account", new BigDecimal("1000.00")));
    }

    @Test
    void shouldReturnEmptySummaryWhenNoTransactionsExist() {
        SummaryDto summary = summaryService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getExpensesByCategory()).isEmpty();
    }

    @Test
    void shouldHandleSummaryWithOnlyExpenses() {
        // Given
        Category utilities = categoryRepository.save(new Category(null, "Utilities"));
        Transaction exp = new Transaction(null, new BigDecimal("120.50"), TransactionType.EXPENSE, utilities, "Electricity", LocalDate.now(), sharedAccount);
        transactionRepository.save(exp);

        // When
        SummaryDto summary = summaryService.getSummary();

        // Then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("120.50"));
        assertThat(summary.getExpensesByCategory()).hasSize(1);
        assertThat(summary.getExpensesByCategory().get(0).totalExpenses()).isEqualByComparingTo(new BigDecimal("120.50"));
    }

    @Test
    void shouldMaintainPrecisePrecisionForDecimalValues() {
        // Given
        Category precisionCat = categoryRepository.save(new Category(null, "Small Expenses"));
        Transaction exp1 = new Transaction(null, new BigDecimal("10.003"), TransactionType.EXPENSE, precisionCat, "Cent 1", LocalDate.now(), sharedAccount);
        Transaction exp2 = new Transaction(null, new BigDecimal("20.007"), TransactionType.EXPENSE, precisionCat, "Cent 2", LocalDate.now(), sharedAccount);
        transactionRepository.saveAll(List.of(exp1, exp2));

        // When
        SummaryDto summary = summaryService.getSummary();

        // Then
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("30.010"));
    }

    @Test
    void shouldCorrectlyCalculateSummaryAndGroupExpensesByCategory() {
        // Given
        Category food = categoryRepository.save(new Category(null, "Food"));
        Category transport = categoryRepository.save(new Category(null, "Transport"));
        Category incomeCat = categoryRepository.save(new Category(null, "Income"));

        Transaction inc1 = new Transaction(null, new BigDecimal("2000.00"), TransactionType.INCOME, incomeCat, "Salary", LocalDate.now(), sharedAccount);
        Transaction exp1 = new Transaction(null, new BigDecimal("150.00"), TransactionType.EXPENSE, food, "Groceries", LocalDate.now(), sharedAccount);
        Transaction exp2 = new Transaction(null, new BigDecimal("300.00"), TransactionType.EXPENSE, transport, "Fuel", LocalDate.now(), sharedAccount);
        transactionRepository.saveAll(List.of(inc1, exp1, exp2));

        // When
        SummaryDto summary = summaryService.getSummary();

        // Then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(summary.getExpensesByCategory()).hasSize(2);
    }
}