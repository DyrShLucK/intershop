package com.intershop.intershop.controller;

import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

public class CartItemServiceTest {

    @InjectMocks
    private CartItemService cartItemService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCartQuantitiesMap_ShouldReturnMapOfProductIdsToQuantities() {
        List<CartItem> cartItems = Arrays.asList(
                new CartItem(1L, 100L, 2),
                new CartItem(2L, 200L, 5)
        );

        when(cartItemRepository.findAll()).thenReturn(Flux.fromIterable(cartItems));

        // Act & Assert
        StepVerifier.create(cartItemService.getCartQuantitiesMap())
                .expectNextMatches(map -> {
                    Map<Long, Integer> expectedMap = new HashMap<>();
                    expectedMap.put(100L, 2);
                    expectedMap.put(200L, 5);
                    return map.equals(expectedMap);
                })
                .verifyComplete();
    }

    @Test
    void getCartQuantitiesMap_EmptyCart_ShouldReturnEmptyMap() {
        // Arrange
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(cartItemService.getCartQuantitiesMap())
                .expectNext(Collections.emptyMap())
                .verifyComplete();
    }

    @Test
    void updateCartItem_PlusAction_ShouldIncreaseQuantity() {
        // Arrange
        Product product = new Product(100L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);
        CartItem existingItem = new CartItem(1L, 100L, 2);

        when(productService.getProduct(100L)).thenReturn(Mono.just(product));
        when(cartItemRepository.findByProductId(100L)).thenReturn(Mono.just(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(new CartItem(1L, 100L, 3)));

        // Act
        StepVerifier.create(cartItemService.updateCartItem(100L, "plus"))
                .verifyComplete();

        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 3));
    }

    @Test
    void updateCartItem_MinusAction_ShouldDecreaseQuantity() {
        Product product = new Product(100L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);
        CartItem existingItem = new CartItem(1L, 100L, 2);

        when(productService.getProduct(100L)).thenReturn(Mono.just(product));
        when(cartItemRepository.findByProductId(100L)).thenReturn(Mono.just(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(new CartItem(1L, 100L, 1)));

        StepVerifier.create(cartItemService.updateCartItem(100L, "minus"))
                .verifyComplete();

        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 1));
    }


    @Test
    void updateCartItem_ProductNotFound_ShouldThrowException() {
        when(productService.getProduct(999L)).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.updateCartItem(999L, "plus"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getProductsInCart_ShouldReturnListOfProducts() {
        List<CartItem> cartItems = Arrays.asList(
                new CartItem(1L, 100L, 2),
                new CartItem(2L, 200L, 1)
        );

        Product product1 = new Product(100L, "Product 1", "Desc", BigDecimal.TEN, new byte[0]);
        Product product2 = new Product(200L, "Product 2", "Desc", BigDecimal.valueOf(20), new byte[0]);

        when(cartItemRepository.findAll()).thenReturn(Flux.fromIterable(cartItems));
        when(productService.getProduct(100L)).thenReturn(Mono.just(product1));
        when(productService.getProduct(200L)).thenReturn(Mono.just(product2));

        StepVerifier.create(cartItemService.getProductsInCart())
                .expectNext(product1, product2)
                .verifyComplete();
    }

    @Test
    void getTotal_ShouldReturnTotalPrice() {
        when(cartItemRepository.calculateTotalPrice()).thenReturn(Mono.just(BigDecimal.valueOf(30)));

        StepVerifier.create(cartItemService.getTotal())
                .expectNext(BigDecimal.valueOf(30))
                .verifyComplete();
    }

}