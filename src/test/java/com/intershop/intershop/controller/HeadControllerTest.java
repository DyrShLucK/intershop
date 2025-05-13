package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HeadController.class)
@ActiveProfiles("test")
public class HeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartItemService cartItemService;

    private Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setImage("test_image".getBytes());
        return product;
    }

    @Test
    public void mainPage_ShouldReturnMainTemplateWithProducts() throws Exception {
        Product product = createTestProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productService.getProductsWithPaginationAndSort(
                "", "", 0, 10, "id", "ASC")).thenReturn(productPage);

        when(cartItemService.getCartQuantitiesMap()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/intershop"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items", "paging", "productQuantities"));
    }

    @Test
    public void mainPage_WithParams_ShouldPassToProductService() throws Exception {
        Product product = createTestProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productService.getProductsWithPaginationAndSort(
                "abc", "abc", 1, 5, "name", "DESC")).thenReturn(productPage);

        when(cartItemService.getCartQuantitiesMap()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/intershop")
                        .param("search", "abc")
                        .param("sort", "name")
                        .param("sortDir", "DESC")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));

        verify(productService, times(1)).getProductsWithPaginationAndSort(
                "abc", "abc", 1, 5, "name", "DESC");
    }

    @Test
    public void getProductImage_ReturnsImageAsBytes() throws Exception {
        Product product = createTestProduct();

        when(productService.getProduct(1L)).thenReturn(product);

        mockMvc.perform(get("/intershop/products/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(product.getImage()));
    }
}