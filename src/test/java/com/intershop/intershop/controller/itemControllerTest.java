package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(itemController.class)
@ActiveProfiles("test")
public class itemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartItemService cartItemService;

    private Product createTestProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.00));
        return product;
    }

    @Test
    public void itemView_ShouldReturnItemTemplateWithAttributes() throws Exception {
        Product product = createTestProduct(1L);

        when(productService.getProduct(1L)).thenReturn(product);
        when(cartItemService.getQuantityByProductId(1L)).thenReturn(3);

        mockMvc.perform(get("/intershop/item/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", product))
                .andExpect(model().attribute("quantity", 3));

        verify(productService, times(1)).getProduct(1L);
        verify(cartItemService, times(1)).getQuantityByProductId(1L);
    }
}