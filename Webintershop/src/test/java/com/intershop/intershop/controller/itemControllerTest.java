package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = itemController.class)
public class itemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartItemService cartItemService;

    private final Product testProduct = new Product(
            1L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);

    @Test
    @DisplayName("Отображение страницы товара с данными")
    @WithMockUser(roles = "USER")
    void getItem_ShouldReturnItemPageWithProductAndQuantity() {
        when(productService.getProduct(1L))
                .thenReturn(Mono.just(testProduct));
        when(cartItemService.getQuantityByProductId(1L))
                .thenReturn(Mono.just(2));

        webTestClient.get()
                .uri("/intershop/item/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();

                    assertThat(body).contains("Test Product");
                    assertThat(body).contains("10 руб.");
                    assertThat(body).contains("Description");
                    assertThat(body).contains("2");
                    assertThat(body).contains("/intershop/products/1/image");
                });
    }

}