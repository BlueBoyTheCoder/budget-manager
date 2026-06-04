package com.example.budgetmanager.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategoriesWithStatus200() throws Exception {
        // Given
        List<CategoryDto> categories = List.of(
                new CategoryDto(1L, "Food"),
                new CategoryDto(2L, "Bills")
        );
        when(categoryService.getAll()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Bills"));
    }

    @Test
    void shouldCreateCategoryAndReturnStatus201() throws Exception {
        // Given
        CategoryDto inputDto = new CategoryDto(null, "Entertainment");
        CategoryDto savedDto = new CategoryDto(5L, "Entertainment");
        when(categoryService.create(inputDto)).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("Entertainment"));
    }

    @Test
    void shouldReturnStatus409WhenCategoryAlreadyExistsOnCreate() throws Exception {
        // Given
        CategoryDto inputDto = new CategoryDto(null, "Food");
        when(categoryService.create(inputDto))
                .thenThrow(new IllegalStateException("Category with name 'Food' already exists"));

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnStatus404WhenCategoryNotFound() throws Exception {
        // Given
        when(categoryService.create(new CategoryDto(null, "Unknown")))
                .thenThrow(new EntityNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryDto(null, "Unknown"))))
                .andExpect(status().isNotFound());
    }
}