package com.example.budgetmanager.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldReturnAllTransactionsWithStatus200() throws Exception {
        // Given
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, new BigDecimal("150.00"), TransactionType.EXPENSE, 2L, "Groceries", LocalDate.now(), 3L, null)
        );

        when(transactionService.getFilteredTransactions(null, null, null)).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(150.00))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));
    }

    @Test
    void shouldReturnFilteredTransactionsWithStatus200() throws Exception {
        // Given
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, new BigDecimal("2000.00"), TransactionType.INCOME, 2L, "Salary", LocalDate.now(), 3L, null)
        );

        when(transactionService.getFilteredTransactions(null, null, "Salary")).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .param("categoryName", "Salary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].description").value("Salary"));
    }

    @Test
    void shouldCreateTransactionAndReturnStatus201() throws Exception {
        // Given
        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("50.00"), TransactionType.EXPENSE, 2L, "Coffee", LocalDate.now(), 3L, null);
        TransactionDto savedDto = new TransactionDto(10L, new BigDecimal("50.00"), TransactionType.EXPENSE, 2L, "Coffee", LocalDate.now(), 3L, null);
        when(transactionService.create(any(TransactionDto.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldDeleteTransactionAndReturnStatus204() throws Exception {
        // Given
        doNothing().when(transactionService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).delete(1L);
    }

    @Test
    void shouldReturnStatus404WhenAccountOrCategoryNotFoundOnCreate() throws Exception {
        // Given
        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("10.00"), TransactionType.EXPENSE, 99L, "Test", LocalDate.now(), 3L, null);
        when(transactionService.create(any(TransactionDto.class)))
                .thenThrow(new EntityNotFoundException("Category not found with id: 99"));

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatus404WhenDeletingNonExistingTransaction() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Transaction not found with id: 99"))
                .when(transactionService).delete(99L);

        // When & Then
        mockMvc.perform(delete("/api/transactions/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatus400WhenAmountIsZeroOrNegativeOnCreate() throws Exception {
        // Given
        TransactionDto invalidInputDto = new TransactionDto(
                null,
                new BigDecimal("-10.00"),
                TransactionType.EXPENSE,
                2L,
                "Invalid Amount Test",
                LocalDate.now(),
                3L,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInputDto)))
                .andExpect(status().isBadRequest());

        // Verification
        verify(transactionService, never()).create(any());
    }

    @Test
    void shouldCreateTransactionWithWarningWhenBudgetLimitIsExceeded() throws Exception {
        // Given
        String warningMsg = "Warning: Budget limit exceeded for category 'Food'!";
        TransactionDto inputDto = new TransactionDto(null, new BigDecimal("600.00"), TransactionType.EXPENSE, 1L, "Big Party Shop", LocalDate.now(), 2L, null);
        TransactionDto savedDtoWithWarning = new TransactionDto(15L, new BigDecimal("600.00"), TransactionType.EXPENSE, 1L, "Big Party Shop", LocalDate.now(), 2L, warningMsg);

        when(transactionService.create(any(TransactionDto.class))).thenReturn(savedDtoWithWarning);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.warningMessage").value(warningMsg));
    }
}