package com.intershop.intershop.service;

import com.intershop.intershop.model.Cart;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;

class CartItemServiceTest {

    private CartItemRepository cartItemRepository;
    private ProductService productService;
    private CartItemService cartItemService;
    private CartService cartService;
    private Principal principal;

    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.valueOf(10), new byte[0]);
    private final CartItem cartItem = new CartItem(1L, 1L, 2,1L);

    @BeforeEach
    void setUp() {
        cartItemRepository = mock(CartItemRepository.class);
        productService = mock(ProductService.class);
        PayService payService = mock(PayService.class);
        cartService = mock(CartService.class);
        cartItemService = new CartItemService(cartItemRepository, productService, payService, cartService);

    }

    @Test
    void getCart_ShouldReturnItems() {
        when(cartService.getCartItemsByUserName("user")).thenReturn(Flux.just(cartItem));

        StepVerifier.create(cartItemService.getCart("user"))
                .expectNextMatches(item -> item.getProductId().equals(1L) && item.getQuantity() == 2)
                .verifyComplete();
    }

    @Test
    void deleteAll_ShouldCallDeleteAll() {
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.deleteAll())
                .verifyComplete();

        verify(cartItemRepository, times(1)).deleteAll();
    }

    @Test
    void getQuantityByProductId_ShouldReturnQuantity() {
        when(cartItemRepository.findByProductId(1L)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartItemService.getQuantityByProductId(1L))
                .expectNext(2)
                .verifyComplete();
    }



    @Test
    @DisplayName("Получение списка продуктов в корзине")
    void getProductsInCart_ShouldReturnProducts() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem));
        when(productService.getProduct(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartItemService.getProductsInCart())
                .expectNextMatches(product -> product.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Получение общей стоимости товаров в корзине")
    void getTotal_ShouldReturnTotalPrice() {
        BigDecimal total = BigDecimal.valueOf(20);
        when(cartItemRepository.calculateTotalPrice()).thenReturn(Mono.just(total));

        StepVerifier.create(cartItemService.getTotal())
                .expectNext(total)
                .verifyComplete();
    }
}