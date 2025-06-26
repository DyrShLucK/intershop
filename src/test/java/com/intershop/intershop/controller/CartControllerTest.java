package com.intershop.intershop.controller;

import com.intershop.intershop.Configuration.SecurityConfig;
import com.intershop.intershop.DTO.CartViewModel;
import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.ContentDisposition.formData;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
@DirtiesContext
class CartControllerTest {

    @MockBean
    private CartService cartService;

    @MockBean
    private CartItemService cartItemService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private PayService payService;

    @MockBean
    private OrderItemService orderItemService;

    @Autowired
    private WebTestClient webTestClient;

    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);


    @Test
    @DisplayName("успешное получение корзины")
    @WithMockUser(roles = "USER")
    void getCart_ShouldReturnCartPage() {
        CartViewModel viewModel = new CartViewModel(
                List.of(testProduct),
                Map.of(1L, 2),
                BigDecimal.valueOf(20),
                100.0f,
                true,
                true
        );

        when(cartService.getCartViewModel(any(Principal.class))).thenReturn(Mono.just(viewModel));

        webTestClient
                .get()
                .uri("/intershop/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assertThat(body).contains("Test Product");
                    assertThat(body).contains("20");
                });
    }

    @Test
    @DisplayName(" добавление товара")
    @WithMockUser(roles = "USER")
    void updateCartItem_AddItem() {
        when(cartService.updateCart(anyLong(), anyString(), any(Principal.class)))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "plus");
        formData.add("redirectUrl", "/test");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/intershop/cart/1")
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/test");
    }

    @Test
    @DisplayName("пустая корзина")
    @WithMockUser(roles = "USER")
    void getCart_EmptyCart() {
        CartViewModel viewModel = new CartViewModel(
                List.of(),
                Map.of(),
                BigDecimal.ZERO,
                0.0f,
                false,
                false
        );

        when(cartService.getCartViewModel(any(Principal.class))).thenReturn(Mono.just(viewModel));

        webTestClient.get()
                .uri("/intershop/cart")
                .exchange()
                .expectStatus().isOk();
    }
    @Test
    @DisplayName("успешное создание заказа")
    @WithMockUser(roles = "USER")
    void createOrder_Success() {
        Order order = new Order();
        order.setId(1L);

        when(orderService.createOrderFromCart(any(Principal.class)))
                .thenReturn(Mono.just(order));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                .uri("/intershop/cart/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/intershop/orders/1");
    }
}