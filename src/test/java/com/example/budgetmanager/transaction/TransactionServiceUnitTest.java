package com.example.budgetmanager.transaction;

import com.example.budgetmanager.account.Account;
import com.example.budgetmanager.account.AccountRepository;
import com.example.budgetmanager.category.Category;
import com.example.budgetmanager.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldIncreaseAccountBalanceWhenCreatingIncomeTransaction() {
        // Given
        Account account = new Account(1L, "Main Account", new BigDecimal("1000.00"));
        Category category = new Category(2L, "Salary", null);

        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("500.00"), TransactionType.INCOME, 2L, "Salary bonus", LocalDate.now(), 1L, null);

        Transaction savedTransaction = new Transaction(100L, new BigDecimal("500.00"), TransactionType.INCOME, category, "Salary bonus", LocalDate.now(), account);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        TransactionDto result = transactionService.create(inputDto);

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getId()).isEqualTo(100L);

        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldDecreaseAccountBalanceWhenCreatingExpenseTransaction() {
        // Given
        Account account = new Account(1L, "Main Account", new BigDecimal("1000.00"));
        Category category = new Category(2L, "Food", null);

        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("200.00"), TransactionType.EXPENSE, 2L, "Groceries", LocalDate.now(), 1L, null);
        Transaction savedTransaction = new Transaction(101L, new BigDecimal("200.00"), TransactionType.EXPENSE, category, "Groceries", LocalDate.now(), account);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        transactionService.create(inputDto);

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundOnCreate() {
        // Given
        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("100.00"), TransactionType.INCOME, 2L, "Test", LocalDate.now(), 99L, null);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(inputDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Account not found with id: 99");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldRevertBalanceAndIncreaseItWhenDeletingExpenseTransaction() {
        // Given
        Account account = new Account(1L, "Main Account", new BigDecimal("800.00"));
        Category category = new Category(2L, "Food", null);
        Transaction existingExpense = new Transaction(100L, new BigDecimal("200.00"), TransactionType.EXPENSE, category, "Groceries", LocalDate.now(), account);

        when(transactionRepository.findById(100L)).thenReturn(Optional.of(existingExpense));

        // When
        transactionService.delete(100L);

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).delete(existingExpense);
    }
}