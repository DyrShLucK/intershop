package com.intershop.intershop.service;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class CartItemServiceTest {

    private CartItemRepository cartItemRepository;
    private ProductService productService;
    private CartItemService cartItemService;
    private PayService payService;

    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.valueOf(10), new byte[0]);
    private final CartItem cartItem = new CartItem(1L, 1L, 2);

    @BeforeEach
    void setUp() {
        cartItemRepository = mock(CartItemRepository.class);
        productService = mock(ProductService.class);
        payService = mock(PayService.class);
        cartItemService = new CartItemService(cartItemRepository, productService, payService);
    }

    @Test
    void getCart_ShouldReturnItems() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem));

        StepVerifier.create(cartItemService.getCart())
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
    @DisplayName("Обновление количества товара (plus)")
    void updateCartItem_PlusAction_ShouldIncreaseQuantity() {
        when(productService.getProduct(1L)).thenReturn(Mono.just(testProduct));
        when(cartItemRepository.findByProductId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(new CartItem(1L, 1L, 3)));

        StepVerifier.create(cartItemService.updateCartItem(1L, "plus"))
                .verifyComplete();

        verify(cartItemRepository, times(1)).save(argThat(item -> item.getQuantity() == 3));
    }

    @Test
    void updateCartItem_MinusToZero_ShouldDeleteItem() {
        CartItem cartItemWithOne = new CartItem(1L, 1L, 1);
        when(productService.getProduct(1L)).thenReturn(Mono.just(testProduct));
        when(cartItemRepository.findByProductId(1L)).thenReturn(Mono.just(cartItemWithOne));
        when(cartItemRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.updateCartItem(1L, "minus"))
                .verifyComplete();

        verify(cartItemRepository, times(1)).deleteById(1L);
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