package com.example.budgetmanager.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldReturnAllAccountsWithStatus200() throws Exception {
        // Given
        List<AccountDto> accounts = List.of(
                new AccountDto(1L, "Main Account", new BigDecimal("1000.00")),
                new AccountDto(2L, "Savings", new BigDecimal("5000.00"))
        );
        when(accountService.getAllAccounts()).thenReturn(accounts);

        // When & Then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Main Account"))
                .andExpect(jsonPath("$[1].name").value("Savings"));
    }

    @Test
    void shouldReturnAccountByIdWithStatus200() throws Exception {
        // Given
        AccountDto account = new AccountDto(1L, "Main Account", new BigDecimal("1000.00"));
        when(accountService.getAccountById(1L)).thenReturn(account);

        // When & Then
        mockMvc.perform(get("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Main Account"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void shouldCreateAccountAndReturnStatus201() throws Exception {
        // Given
        AccountDto inputDto = new AccountDto(null, "New Account", new BigDecimal("500.00"));
        AccountDto savedDto = new AccountDto(10L, "New Account", new BigDecimal("500.00"));
        when(accountService.createAccount(inputDto)).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("New Account"));
    }

    @Test
    void shouldDeleteAccountAndReturnStatus204() throws Exception {
        // Given
        doNothing().when(accountService).deleteAccount(1L);

        // When & Then
        mockMvc.perform(delete("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(1L);
    }

    @Test
    void shouldReturnStatus404WhenAccountNotFoundById() throws Exception {
        // Given
        when(accountService.getAccountById(99L))
                .thenThrow(new EntityNotFoundException("Account not found with ID: 99"));

        // When & Then
        mockMvc.perform(get("/api/accounts/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatus409WhenDeletingAccountWithActiveTransactions() throws Exception {
        // Given
        doThrow(new IllegalStateException("Cannot delete account with active transactions."))
                .when(accountService).deleteAccount(1L);

        // When & Then
        mockMvc.perform(delete("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnStatus404WhenDeletingNonExistingAccount() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Cannot delete. Account not found with ID: 99"))
                .when(accountService).deleteAccount(99L);

        // When & Then
        mockMvc.perform(delete("/api/accounts/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldExportTransactionsToCsvWithStatus200() throws Exception {
        // Given
        Long accountId = 1L;
        String mockCsv = "ID;Amount;Type;Description;Date;Category ID\n1;150.00;EXPENSE;Groceries;2026-06-04;3\n";
        when(accountService.exportTransactionsToCsv(accountId)).thenReturn(mockCsv);

        // When & Then
        mockMvc.perform(get("/api/accounts/1/transactions/export"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assert contentType != null && contentType.contains("text/csv");
                })
                .andExpect(result -> {
                    String header = result.getResponse().getHeader("Content-Disposition");
                    assert header != null && header.contains("attachment; filename=\"transactions_account_1.csv\"");
                })
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assert content.equals(mockCsv);
                });
    }

    @Test
    void shouldReturnStatus404WhenExportingTransactionsForNonExistingAccount() throws Exception {
        // Given
        Long nonExistingId = 99L;
        when(accountService.exportTransactionsToCsv(nonExistingId))
                .thenThrow(new EntityNotFoundException("Account not found with ID: 99"));

        // When & Then
        mockMvc.perform(get("/api/accounts/99/transactions/export"))
                .andExpect(status().isNotFound());
    }
}