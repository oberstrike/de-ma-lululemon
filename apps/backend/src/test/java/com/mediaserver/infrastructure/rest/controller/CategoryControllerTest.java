package com.mediaserver.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.application.usecase.category.*;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.domain.model.Category;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDTO;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDTO;
import com.mediaserver.infrastructure.rest.mapper.CategoryRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CategoryController in Clean Architecture. Tests the REST adapter layer that uses
 * application services (use cases).
 */
@WebMvcTest(CategoryController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private GetAllCategoriesUseCase getAllCategoriesUseCase;

    @MockitoBean private GetCategoryUseCase getCategoryUseCase;

    @MockitoBean private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean private UpdateCategoryUseCase updateCategoryUseCase;

    @MockitoBean private DeleteCategoryUseCase deleteCategoryUseCase;

    @MockitoBean private CategoryRestMapper categoryRestMapper;

    private Category domainCategory;
    private CategoryResponseDTO categoryResponseDto;

    @BeforeEach
    void setUp() {
        domainCategory =
                Category.builder()
                        .id("cat-1")
                        .name("Action")
                        .description("Action movies")
                        .sortOrder(1)
                        .build();

        categoryResponseDto =
                CategoryResponseDTO.builder()
                        .id("cat-1")
                        .name("Action")
                        .description("Action movies")
                        .sortOrder(1)
                        .movieCount(5)
                        .build();
    }

    @Test
    void getAllCategories_shouldReturnCategoryList() throws Exception {
        when(getAllCategoriesUseCase.getAllCategories()).thenReturn(List.of(domainCategory));
        when(categoryRestMapper.toResponse(domainCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"))
                .andExpect(jsonPath("$[0].name").value("Action"))
                .andExpect(jsonPath("$[0].movieCount").value(5));

        verify(getAllCategoriesUseCase).getAllCategories();
    }

    @Test
    void getCategory_shouldReturnCategory_whenExists() throws Exception {
        when(getCategoryUseCase.getCategory("cat-1")).thenReturn(domainCategory);
        when(categoryRestMapper.toResponse(domainCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(get("/api/categories/cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat-1"))
                .andExpect(jsonPath("$.name").value("Action"))
                .andExpect(jsonPath("$.movieCount").value(5));

        verify(getCategoryUseCase).getCategory("cat-1");
    }

    @Test
    void getCategory_shouldReturn404_whenNotFound() throws Exception {
        when(getCategoryUseCase.getCategory("nonexistent"))
                .thenThrow(new CategoryNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/categories/nonexistent")).andExpect(status().isNotFound());
    }

    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(2)
                        .build();

        CreateCategoryCommand command =
                CreateCategoryCommand.builder()
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(2)
                        .build();

        Category savedCategory =
                Category.builder()
                        .id("cat-2")
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(2)
                        .build();

        CategoryResponseDTO createdDto =
                CategoryResponseDTO.builder()
                        .id("cat-2")
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(2)
                        .movieCount(0)
                        .build();

        when(categoryRestMapper.toCreateCommand(request)).thenReturn(command);
        when(createCategoryUseCase.createCategory(any(CreateCategoryCommand.class)))
                .thenReturn(savedCategory);
        when(categoryRestMapper.toResponse(savedCategory)).thenReturn(createdDto);

        mockMvc.perform(
                        post("/api/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cat-2"))
                .andExpect(jsonPath("$.name").value("Comedy"));

        verify(categoryRestMapper).toCreateCommand(request);
        verify(createCategoryUseCase).createCategory(any(CreateCategoryCommand.class));
    }

    @Test
    void createCategory_shouldReturn400_whenNameMissing() throws Exception {
        CategoryRequestDTO request =
                CategoryRequestDTO.builder().description("Some description").build();

        mockMvc.perform(
                        post("/api/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("Updated Action")
                        .description("Updated description")
                        .sortOrder(10)
                        .build();

        UpdateCategoryCommand command =
                UpdateCategoryCommand.builder()
                        .id("cat-1")
                        .name("Updated Action")
                        .description("Updated description")
                        .sortOrder(10)
                        .build();

        Category updatedCategory =
                Category.builder()
                        .id("cat-1")
                        .name("Updated Action")
                        .description("Updated description")
                        .sortOrder(10)
                        .build();

        CategoryResponseDTO updatedDto =
                categoryResponseDto.toBuilder()
                        .name("Updated Action")
                        .description("Updated description")
                        .sortOrder(10)
                        .build();

        when(categoryRestMapper.toUpdateCommand("cat-1", request)).thenReturn(command);
        when(updateCategoryUseCase.updateCategory(any(UpdateCategoryCommand.class)))
                .thenReturn(updatedCategory);
        when(categoryRestMapper.toResponse(updatedCategory)).thenReturn(updatedDto);

        mockMvc.perform(
                        put("/api/categories/cat-1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Action"));

        verify(updateCategoryUseCase).updateCategory(any(UpdateCategoryCommand.class));
    }

    @Test
    void deleteCategory_shouldReturn204() throws Exception {
        doNothing().when(deleteCategoryUseCase).deleteCategory("cat-1");

        mockMvc.perform(delete("/api/categories/cat-1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteCategoryUseCase).deleteCategory("cat-1");
    }

    @Test
    void getAllCategories_shouldReturnSortedCategories() throws Exception {
        Category category2 = Category.builder().id("cat-2").name("Drama").sortOrder(2).build();

        CategoryResponseDTO categoryResponseDto2 =
                CategoryResponseDTO.builder()
                        .id("cat-2")
                        .name("Drama")
                        .sortOrder(2)
                        .movieCount(3)
                        .build();

        when(getAllCategoriesUseCase.getAllCategories())
                .thenReturn(List.of(domainCategory, category2));
        when(categoryRestMapper.toResponse(domainCategory)).thenReturn(categoryResponseDto);
        when(categoryRestMapper.toResponse(category2)).thenReturn(categoryResponseDto2);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sortOrder").value(1))
                .andExpect(jsonPath("$[1].sortOrder").value(2));
    }

    @Test
    void createCategory_shouldHandleNullOptionalFields() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder().name("Minimal Category").build();

        CreateCategoryCommand command =
                CreateCategoryCommand.builder().name("Minimal Category").build();

        Category savedCategory = Category.builder().id("cat-3").name("Minimal Category").build();

        CategoryResponseDTO createdDto =
                CategoryResponseDTO.builder()
                        .id("cat-3")
                        .name("Minimal Category")
                        .movieCount(0)
                        .build();

        when(categoryRestMapper.toCreateCommand(request)).thenReturn(command);
        when(createCategoryUseCase.createCategory(any(CreateCategoryCommand.class)))
                .thenReturn(savedCategory);
        when(categoryRestMapper.toResponse(savedCategory)).thenReturn(createdDto);

        mockMvc.perform(
                        post("/api/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cat-3"))
                .andExpect(jsonPath("$.name").value("Minimal Category"));
    }
}
