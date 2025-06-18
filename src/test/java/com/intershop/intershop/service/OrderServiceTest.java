package com.intershop.intershop.service;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CartItemService cartItemService;
    private OrderItemService orderItemService;
    private ProductService productService;
    private OrderService orderService;
    private PayService payService;

    private final CartItem cartItem = new CartItem(null, 1L, 2);
    private final Product product = new Product(1L, "Test Product", "Desc", BigDecimal.TEN, new byte[0]);
    private final OrderItem orderItem = new OrderItem(null, 1L, 1L, 2, BigDecimal.TEN, new Product());
    private final Order order = new Order();

    {
        order.setId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.TEN);
    }

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        cartItemService = mock(CartItemService.class);
        orderItemService = mock(OrderItemService.class);
        productService = mock(ProductService.class);
        payService = mock(PayService.class);
        orderService = new OrderService(orderRepository, cartItemService, orderItemService, productService, payService);
    }

    @Test
    void createOrderFromCart_ShouldCreateOrderAndItems() {
        when(cartItemService.getCart()).thenReturn(Flux.just(cartItem));
        when(productService.getProduct(1L)).thenReturn(Mono.just(product));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemService.saveAll(any(Flux.class))).thenReturn(Flux.just(orderItem));
        when(payService.processPayment(anyDouble())).thenReturn(Mono.just(true));

        StepVerifier.create(orderService.createOrderFromCart())
                .expectNextMatches(o -> o.getId() == 1L && !o.getOrderItems().isEmpty())
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    void findAll_ShouldReturnOrdersWithItems() {
        when(orderRepository.findAllByOrderByIdDesc()).thenReturn(Flux.just(order));
        when(orderItemService.getOrderItemsByOrderId(1L)).thenReturn(Flux.just(orderItem));

        StepVerifier.create(orderService.findAll())
                .expectNextMatches(o -> o.getId() == 1L && !o.getOrderItems().isEmpty())
                .verifyComplete();

        verify(orderItemService, times(1)).getOrderItemsByOrderId(1L);
    }

    @Test
    void findOrderById_ShouldReturnOrderWithItems() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemService.getOrderItemsByOrderId(1L)).thenReturn(Flux.just(orderItem));

        StepVerifier.create(orderService.findOrderById(1L))
                .expectNextMatches(o -> o.getId() == 1L && !o.getOrderItems().isEmpty())
                .verifyComplete();

        verify(orderRepository, times(1)).findById(1L);
    }
}