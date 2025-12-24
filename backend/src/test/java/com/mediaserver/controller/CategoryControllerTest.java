package com.mediaserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.dto.CategoryCreateRequest;
import com.mediaserver.dto.CategoryDto;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = CategoryDto.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .sortOrder(1)
                .movieCount(5)
                .build();
    }

    @Test
    void getAllCategories_shouldReturnCategoryList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategoryDto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"))
                .andExpect(jsonPath("$[0].name").value("Action"))
                .andExpect(jsonPath("$[0].movieCount").value(5));
    }

    @Test
    void getCategory_shouldReturnCategory_whenExists() throws Exception {
        when(categoryService.getCategory("cat-1")).thenReturn(testCategoryDto);

        mockMvc.perform(get("/api/categories/cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat-1"))
                .andExpect(jsonPath("$.name").value("Action"));
    }

    @Test
    void getCategory_shouldReturn404_whenNotFound() throws Exception {
        when(categoryService.getCategory("nonexistent"))
                .thenThrow(new CategoryNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/categories/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Comedy")
                .description("Comedy movies")
                .sortOrder(2)
                .build();

        CategoryDto created = CategoryDto.builder()
                .id("cat-2")
                .name("Comedy")
                .description("Comedy movies")
                .sortOrder(2)
                .movieCount(0)
                .build();

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cat-2"))
                .andExpect(jsonPath("$.name").value("Comedy"));
    }

    @Test
    void createCategory_shouldReturn400_whenNameMissing() throws Exception {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .description("Some description")
                .build();

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Updated Action")
                .description("Updated description")
                .sortOrder(10)
                .build();

        testCategoryDto.setName("Updated Action");
        when(categoryService.updateCategory(eq("cat-1"), any(CategoryCreateRequest.class)))
                .thenReturn(testCategoryDto);

        mockMvc.perform(put("/api/categories/cat-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Action"));
    }

    @Test
    void deleteCategory_shouldReturn204() throws Exception {
        doNothing().when(categoryService).deleteCategory("cat-1");

        mockMvc.perform(delete("/api/categories/cat-1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory("cat-1");
    }
}
