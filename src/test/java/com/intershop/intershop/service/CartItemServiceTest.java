package com.intershop.intershop.service;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartItemService cartItemService;

    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
    }

    @Test
    void getQuantityByProductId_ReturnsQuantity() {
        when(cartItemRepository.findByProduct_Id(1L)).thenReturn(testCartItem);
        assertEquals(2, cartItemService.getQuantityByProductId(1L));
    }


    @Test
    void updateCartItem_Minus_DecreasesQuantity() {
        when(productService.getProduct(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByProduct_Id(1L)).thenReturn(testCartItem);

        cartItemService.updateCartItem(1L, "minus");

        assertEquals(1, testCartItem.getQuantity());
    }

    @Test
    void updateCartItem_DeleteWhenZero() {
        testCartItem.setQuantity(1);
        when(productService.getProduct(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByProduct_Id(1L)).thenReturn(testCartItem);

        cartItemService.updateCartItem(1L, "minus");

        verify(cartItemRepository).deleteById(1L);
    }

    @Test
    void getTotal_ReturnsTotalPrice() {
        when(cartItemRepository.calculateTotalPrice()).thenReturn(BigDecimal.valueOf(200.00));
        assertEquals(BigDecimal.valueOf(200.00), cartItemService.getTotal());
    }
}
