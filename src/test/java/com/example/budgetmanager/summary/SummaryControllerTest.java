package com.example.budgetmanager.summary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SummaryController.class)
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SummaryService summaryService;

    @Test
    void shouldReturnSummaryWithStatus200() throws Exception {
        // Given
        List<CategoryOverviewDto> expensesByCategory = List.of(
                new CategoryOverviewDto("Food", new BigDecimal("150.00")),
                new CategoryOverviewDto("Bills", new BigDecimal("300.00"))
        );

        SummaryDto summaryDto = new SummaryDto(
                new BigDecimal("2000.00"),
                new BigDecimal("450.00"),
                expensesByCategory
        );

        when(summaryService.getSummary()).thenReturn(summaryDto);

        // When & Then
        mockMvc.perform(get("/api/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(2000.00))
                .andExpect(jsonPath("$.totalExpenses").value(450.00))
                .andExpect(jsonPath("$.expensesByCategory.size()").value(2))
                .andExpect(jsonPath("$.expensesByCategory[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.expensesByCategory[0].totalExpenses").value(150.00));
    }

    @Test
    void shouldReturnStatus500WhenSummaryServiceFailsUnexpectedly() throws Exception {
        // Given
        // Simulating an unexpected runtime/database exception during calculation
        when(summaryService.getSummary())
                .thenThrow(new RuntimeException("Database connection timeout during aggregation"));

        // When & Then
        mockMvc.perform(get("/api/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}