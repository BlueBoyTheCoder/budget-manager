package com.example.budgetmanager.transaction;

import com.example.budgetmanager.account.Account;
import com.example.budgetmanager.account.AccountRepository;
import com.example.budgetmanager.category.Category;
import com.example.budgetmanager.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Account sharedAccount;
    private Category food;
    private Category bills;

    @BeforeEach
    void setUp() {
        sharedAccount = accountRepository.save(new Account(null, "Wallet", new BigDecimal("1000.00")));
        food = categoryRepository.save(new Category(null, "Food", null));
        bills = categoryRepository.save(new Category(null, "Bills", null));

        Transaction t1 = new Transaction(null, new BigDecimal("50.00"), TransactionType.EXPENSE, food, "Lunch", LocalDate.of(2026, 6, 1), sharedAccount);
        Transaction t2 = new Transaction(null, new BigDecimal("150.00"), TransactionType.EXPENSE, food, "Groceries", LocalDate.of(2026, 6, 3), sharedAccount);
        Transaction t3 = new Transaction(null, new BigDecimal("200.00"), TransactionType.EXPENSE, bills, "Internet", LocalDate.of(2026, 6, 5), sharedAccount);

        transactionRepository.saveAll(List.of(t1, t2, t3));
    }

    @Test
    void shouldReturnAllTransactionsWhenAllFiltersAreNull() {
        // When
        List<Transaction> result = transactionRepository.findFilteredTransactions(null, null, null);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldFilterTransactionsByCategoryNameIgnoreCase() {
        // When
        List<Transaction> result = transactionRepository.findFilteredTransactions(null, null, "food");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getCategory().getName().equals("Food"));
    }

    @Test
    void shouldFilterTransactionsByDateRange() {
        // Given
        LocalDate from = LocalDate.of(2026, 6, 2);
        LocalDate to = LocalDate.of(2026, 6, 6);

        // When
        List<Transaction> result = transactionRepository.findFilteredTransactions(from, to, null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Transaction::getDescription).containsExactlyInAnyOrder("Groceries", "Internet");
    }
}