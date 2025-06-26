package com.intershop.intershop.service;

import com.intershop.intershop.DTO.CartViewModel;
import com.intershop.intershop.exception.CartNotFoundException;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Cart;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import com.intershop.intershop.repository.CartRepository;
import com.intershop.intershop.service.PayService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PayService payService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCartByUserName_ExistingCart() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserName("testUser");
        cart.setTotalAmount(BigDecimal.ZERO);

        when(cartRepository.findByUserName("testUser")).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.getCartByUserName("testUser"))
                .expectNext(cart)
                .verifyComplete();
    }

    @Test
    void testGetCartByUserName_CartNotFound_CreateNew() {
        Cart newCart = new Cart();
        newCart.setId(1L);
        newCart.setUserName("testUser");
        newCart.setTotalAmount(BigDecimal.ZERO);

        when(cartRepository.findByUserName("testUser")).thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(newCart));

        StepVerifier.create(cartService.getCartByUserName("testUser"))
                .expectNext(newCart)
                .verifyComplete();

        verify(cartRepository).save(cartCaptor.capture());
        Cart capturedCart = cartCaptor.getValue();
        assertEquals("testUser", capturedCart.getUserName());
        assertEquals(BigDecimal.ZERO, capturedCart.getTotalAmount());
    }

    // Тесты для getCartItemsByUserName
    @Test
    void testGetCartItemsByUserName() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserName("testUser");

        List<CartItem> cartItems = Arrays.asList(
                new CartItem(1L, 101L, 2, 1L),
                new CartItem(2L, 102L, 1, 1L)
        );

        when(cartRepository.findByUserName("testUser")).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.fromIterable(cartItems));

        StepVerifier.create(cartService.getCartItemsByUserName("testUser"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testUpdateCartTotalAmount() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserName("testUser");

        CartItem item1 = new CartItem(1L, 101L, 2, 1L);
        CartItem item2 = new CartItem(2L, 102L, 3, 1L);

        Product product1 = new Product();
        product1.setId(101L);
        product1.setPrice(BigDecimal.valueOf(10.0));

        Product product2 = new Product();
        product2.setId(102L);
        product2.setPrice(BigDecimal.valueOf(5.0));

        when(cartRepository.findByUserName("testUser")).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(item1, item2));
        when(productService.getProduct(101L)).thenReturn(Mono.just(product1));
        when(productService.getProduct(102L)).thenReturn(Mono.just(product2));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.updateCartTotalAmount("testUser"))
                .verifyComplete();

        verify(cartRepository).save(cartCaptor.capture());
        assertEquals(BigDecimal.valueOf(35.0), cartCaptor.getValue().getTotalAmount());
    }


}