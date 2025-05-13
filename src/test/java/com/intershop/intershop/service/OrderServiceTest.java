package com.intershop.intershop.service;

import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private CartItem cartItem;
    private Order order;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(100.00));

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        order = new Order();
        order.setId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(200.00));
    }

    @Test
    void createOrderFromCart_Successful() {
        when(cartItemService.getCart()).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order createdOrder = orderService.createOrderFromCart();

        assertNotNull(createdOrder);
        assertEquals(BigDecimal.valueOf(200.00), createdOrder.getTotalAmount());
        verify(orderItemService, times(1)).saveAll(anyList());
        verify(cartItemService, times(1)).deleteAll();
    }

    @Test
    void createOrderFromCart_EmptyCart_ThrowsException() {
        when(cartItemService.getCart()).thenReturn(Collections.emptyList());

        assertThrows(CartEmptyException.class, () -> orderService.createOrderFromCart());
    }

    @Test
    void findAll_ReturnsOrderList() {
        when(orderRepository.findAllByOrderByIdDesc()).thenReturn(List.of(order));

        List<Order> orders = orderService.findAll();

        assertNotNull(orders);
        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findAllByOrderByIdDesc();
    }

    @Test
    void findOrderById_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void findOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findOrderById(1L));
    }
}