package com.intershop.intershop.controller;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = CartController.class)
public class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartItemService cartItemService;

    @MockBean
    private OrderService orderService;

    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);

    @Test
    @DisplayName("Отображение страницы корзины с товарами")
    void getCart_ShouldReturnCartPageWithItemsAndTotalPrice() {
        List<Product> products = List.of(testProduct);
        Map<Long, Integer> quantities = Map.of(1L, 2);
        BigDecimal total = BigDecimal.valueOf(20.0);

        when(cartItemService.getProductsInCart()).thenReturn(Flux.fromIterable(products));
        when(cartItemService.getCartQuantitiesMap()).thenReturn(Mono.just(quantities));
        when(cartItemService.getTotal()).thenReturn(Mono.just(total));

        webTestClient.get()
                .uri("/intershop/cart")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();

                    assert body != null;
                    org.assertj.core.api.Assertions.assertThat(body).contains("Test Product");
                    org.assertj.core.api.Assertions.assertThat(body).contains("10 руб.");
                    org.assertj.core.api.Assertions.assertThat(body).contains("2");
                    org.assertj.core.api.Assertions.assertThat(body).contains("20");
                });
    }

    @Test
    @DisplayName("Создание заказа из корзины")
    void createOrder_ShouldCreateOrderAndRedirectToOrderPage() {
        Order testOrder = new Order();
        testOrder.setId(1L);

        when(orderService.createOrderFromCart())
                .thenReturn(Mono.just(testOrder));

        webTestClient.post()
                .uri("/intershop/cart/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/intershop/orders/1");

        verify(orderService, times(1)).createOrderFromCart();
    }

}