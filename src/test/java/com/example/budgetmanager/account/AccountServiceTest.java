package com.example.budgetmanager.account;

import com.example.budgetmanager.transaction.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReturnAllAccountsMappedToDto() {
        // Given
        Account acc1 = new Account(1L, "Main Account", new BigDecimal("1500.00"));
        Account acc2 = new Account(2L, "Savings", new BigDecimal("5000.00"));
        when(accountRepository.findAll()).thenReturn(List.of(acc1, acc2));

        // When
        List<AccountDto> result = accountService.getAllAccounts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("Main Account");
        assertThat(result.getFirst().getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));

        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAccountDtoWhenAccountExists() {
        // Given
        Account account = new Account(1L, "Main Account", new BigDecimal("1000.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountDto result = accountService.getAccountById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Main Account");

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistById() {
        // Given
        when(accountRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountById(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Account not found with ID: 10");
    }

    @Test
    void shouldSaveAndReturnCreatedAccountDto() {
        // Given
        AccountDto inputDto = new AccountDto(null, "New Account", new BigDecimal("500.00"));
        Account savedAccount = new Account(10L, "New Account", new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // When
        AccountDto result = accountService.createAccount(inputDto);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("New Account");
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldDeleteAccountSuccessfullyWhenNoTransactionsExist() {
        // Given
        Account account = new Account(1L, "Empty Account", BigDecimal.ZERO);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.existsByAccountId(1L)).thenReturn(false);

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).existsByAccountId(1L);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void shouldThrowExceptionAndBlockDeletionWhenAccountHasActiveTransactions() {
        // Given
        Account account = new Account(1L, "Active Account", new BigDecimal("250.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.existsByAccountId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> accountService.deleteAccount(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete account with active transactions.");
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundOnDelete() {
        // Given
        when(accountRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.deleteAccount(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot delete. Account not found with ID: 10");

        // Verification
        verify(transactionRepository, never()).existsByAccountId(anyLong());
        verify(accountRepository, never()).delete(any());
    }
}