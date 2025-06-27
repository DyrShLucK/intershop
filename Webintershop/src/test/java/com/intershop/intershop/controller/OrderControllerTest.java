package com.intershop.intershop.controller;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = OrderController.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    private final Order testOrder = createTestOrder();
    private final OrderItem testItem = createTestOrderItem();

    private static Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(20.0));
        order.setUserName("testUser");
        return order;
    }

    private static OrderItem createTestOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.TEN);
        return item;
    }

    @Test
    @DisplayName("Отображение страницы конкретного заказа")
    @WithMockUser(roles = "USER")
    void getOrder_ShouldReturnOrderPageWithItems() {
        when(orderService.findOrderById(1L))
                .thenReturn(Mono.just(testOrder));
        when(orderService.getOrderItems(1L))
                .thenReturn(Flux.just(testItem));

        webTestClient.get()
                .uri("/intershop/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();

                    assertThat(body).contains("Заказ №1");
                    assertThat(body).contains("20.0 руб.");
                });
    }

    @Test
    @WithMockUser(roles = "USER", username = "testUser")
    void getOrders_ShouldReturnOrdersListWithItems() {
        Order anotherOrder = new Order();
        anotherOrder.setId(2L);
        anotherOrder.setOrderDate(LocalDateTime.now().minusDays(1));
        anotherOrder.setTotalAmount(BigDecimal.valueOf(50.0));

        OrderItem anotherItem = new OrderItem();
        anotherItem.setId(2L);
        anotherItem.setProductId(2L);
        anotherItem.setQuantity(5);
        anotherItem.setPrice(BigDecimal.TEN);
        when(orderService.getUserName(any(Principal.class)))
                .thenReturn("testUser");
        when(orderService.findOrdersByUserName(eq("testUser")))
                .thenReturn(Flux.just(testOrder, anotherOrder));

        when(orderService.getOrderItems(Mockito.anyLong()))
                .thenReturn(Flux.just(testItem))
                .thenReturn(Flux.just(anotherItem));

        webTestClient.get()
                .uri("/intershop/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertThat(body).contains("Заказы", "Заказ №1", "Заказ №2");
                });
    }
}