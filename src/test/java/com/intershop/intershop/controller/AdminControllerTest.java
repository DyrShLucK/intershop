package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void showAddProductForm_ShouldReturnAddProductTemplateWithProductAttribute() throws Exception {
        mockMvc.perform(get("/admin/add-product"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    public void saveProduct_ShouldCallServiceAndRedirect() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "TestImageBytes".getBytes());

        mockMvc.perform(multipart("/admin/add-product")
                        .file(image)
                        .param("name", "Test Product")
                        .param("description", "Test Description")
                        .param("price", "199.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/intershop"));

        verify(productService, times(1)).save(any(Product.class));
    }

    @Test
    public void saveProduct_ShouldSaveCorrectProduct() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "TestImageBytes".getBytes());

        mockMvc.perform(multipart("/admin/add-product")
                .file(image)
                .param("name", "Test Product")
                .param("description", "Test Description")
                .param("price", "199.99"));

        verify(productService).save(argThat(product ->
                product.getName().equals("Test Product") &&
                        product.getDescription().equals("Test Description") &&
                        product.getPrice().compareTo(BigDecimal.valueOf(199.99)) == 0 &&
                        Arrays.equals(product.getImage(), "TestImageBytes".getBytes())
        ));
    }
}