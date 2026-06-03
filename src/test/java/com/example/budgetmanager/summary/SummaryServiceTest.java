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

    @BeforeEach
    void setUp() {
        summaryService = new SummaryService(transactionRepository);
    }

    @Test
    void shouldReturnEmptySummaryWhenNoTransactionsExist() {
        // When
        SummaryDto summary = summaryService.getSummary();

        // Then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getExpensesByCategory()).isEmpty();
    }

    @Test
    void shouldCorrectlyCalculateSummaryAndGroupExpensesByCategory() {
        // Given
        Account account = new Account(null, "Main Account", new BigDecimal("5000.00"));
        accountRepository.save(account);

        Category food = new Category(null, "Food");
        Category transport = new Category(null, "Transport");
        Category income = new Category(null, "Income");
        categoryRepository.saveAll(List.of(food, transport, income));

        Transaction inc1 = new Transaction(null, new BigDecimal("2000.00"), TransactionType.INCOME, income, "Salary", LocalDate.now(), account);
        Transaction inc2 = new Transaction(null, new BigDecimal("500.00"), TransactionType.INCOME, income, "Bonus", LocalDate.now(), account);

        Transaction exp1 = new Transaction(null, new BigDecimal("150.00"), TransactionType.EXPENSE, food, "Groceries", LocalDate.now(), account);
        Transaction exp2 = new Transaction(null, new BigDecimal("50.00"), TransactionType.EXPENSE, food, "Dinner", LocalDate.now(), account);
        Transaction exp3 = new Transaction(null, new BigDecimal("300.00"), TransactionType.EXPENSE, transport, "Fuel", LocalDate.now(), account);

        transactionRepository.saveAll(List.of(inc1, inc2, exp1, exp2, exp3));

        // When
        SummaryDto summary = summaryService.getSummary();

        // Then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("500.00"));

        List<CategoryOverviewDto> categoryOverviews = summary.getExpensesByCategory();
        assertThat(categoryOverviews).hasSize(2);

        CategoryOverviewDto foodOverview = categoryOverviews.stream()
                .filter(c -> c.categoryName().equals("Food"))
                .findFirst()
                .orElseThrow();
        assertThat(foodOverview.totalExpenses()).isEqualByComparingTo(new BigDecimal("200.00"));

        CategoryOverviewDto transportOverview = categoryOverviews.stream()
                .filter(c -> c.categoryName().equals("Transport"))
                .findFirst()
                .orElseThrow();
        assertThat(transportOverview.totalExpenses()).isEqualByComparingTo(new BigDecimal("300.00"));
    }
}