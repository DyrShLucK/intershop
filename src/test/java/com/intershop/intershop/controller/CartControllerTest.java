package com.intershop.intershop.controller;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ActiveProfiles("test")
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartItemService cartItemService;

    @MockBean
    private OrderService orderService;

    private Product createTestProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.00));
        return product;
    }

    @Test
    public void cartView_ShouldReturnCartTemplateWithAttributes() throws Exception {
        Product product = createTestProduct(1L);

        when(cartItemService.getProductsInCart()).thenReturn(List.of(product));
        when(cartItemService.getCartQuantitiesMap()).thenReturn(Map.of(1L, 2));
        when(cartItemService.getTotal()).thenReturn(BigDecimal.valueOf(200.00));

        mockMvc.perform(get("/intershop/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(product)))
                .andExpect(model().attribute("productQuantities", Map.of(1L, 2)))
                .andExpect(model().attribute("total", BigDecimal.valueOf(200.00)));

        verify(cartItemService, times(1)).getProductsInCart();
        verify(cartItemService, times(1)).getCartQuantitiesMap();
        verify(cartItemService, times(1)).getTotal();
    }

    @Test
    public void createOrderAndRedirect_Successful_ShouldRedirectToOrderPage() throws Exception {
        when(orderService.createOrderFromCart()).thenReturn(new Order());
        when(orderService.createOrderFromCart()).thenAnswer(invocation -> {
            Order order = new Order();
            order.setId(123L);
            return order;
        });

        mockMvc.perform(post("/intershop/cart/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/intershop/orders/123"));

        verify(orderService, times(1)).createOrderFromCart();
    }

}