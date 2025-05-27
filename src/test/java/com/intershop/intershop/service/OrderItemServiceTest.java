package com.intershop.intershop.service;

import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderItemServiceTest {

    private OrderItemRepository orderItemRepository;
    private ProductService productService;
    private OrderItemService orderItemService;

    private final OrderItem testItem = new OrderItem(null, 1L, 1L, 2, BigDecimal.valueOf(10), new Product());
    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.valueOf(10), new byte[0]);

    @BeforeEach
    void setUp() {
        orderItemRepository = mock(OrderItemRepository.class);
        productService = mock(ProductService.class);
        orderItemService = new OrderItemService(orderItemRepository, productService);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(testItem));

        StepVerifier.create(orderItemService.save(testItem))
                .expectNext(testItem)
                .verifyComplete();

        verify(orderItemRepository, times(1)).save(testItem);
    }

    @Test
    void getOrderItemsByOrderId_ShouldSetProduct() {
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(testItem));
        when(productService.getProduct(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(orderItemService.getOrderItemsByOrderId(1L))
                .expectNextMatches(item -> item.getProduct() != null && item.getProduct().getId().equals(1L))
                .verifyComplete();

        verify(productService, times(1)).getProduct(1L);
    }
}