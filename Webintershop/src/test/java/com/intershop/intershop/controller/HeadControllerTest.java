package com.intershop.intershop.controller;

import com.intershop.intershop.DTO.ProductPageDTO;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.CartService;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = HeadController.class)
public class HeadControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartItemService cartItemService;
    @MockBean
    private Principal principal;

    private final Product testProduct = new Product(1L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);

    @Test
    @WithMockUser(roles = "USER")
    void mainPage_ShouldReturnProductsAndCartData() {
        String search = "test";
        String sort = "name";
        String sortDir = "ASC";
        int pageSize = 5;
        int pageNumber = 1;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDir), sort));
        ProductPageDTO productPageDTO = new ProductPageDTO(
                List.of(testProduct),
                pageable,
                1,
                search
        );

        Map<Long, Integer> cartQuantities = new HashMap<>();
        cartQuantities.put(1L, 2);

        when(productService.getProductsWithPaginationAndSort(
                Mockito.anyString(),
                Mockito.any(Pageable.class)))
                .thenReturn(Mono.just(productPageDTO));

        when(cartItemService.getCartQuantitiesMap(Mockito.any(Principal.class)))
                .thenReturn(Mono.just(cartQuantities));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/intershop")
                        .queryParam("search", search)
                        .queryParam("sort", sort)
                        .queryParam("sortDir", sortDir)
                        .queryParam("pageSize", pageSize)
                        .queryParam("pageNumber", pageNumber)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();

                    assertThat(body).contains("Test Product");
                    assertThat(body).contains("2");
                    assertThat(body).contains("/intershop/products/1/image");
                    assertThat(body).contains("search");
                    assertThat(body).contains("sort");
                    assertThat(body).contains("pageSize");
                });
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductImage_ShouldReturnImage() {

        Product productWithImage = new Product(
                1L, "Test Product", "Description", BigDecimal.TEN, new byte[0]);

        when(productService.getProduct(1L))
                .thenReturn(Mono.just(productWithImage));

        webTestClient.get()
                .uri("/intershop/products/1/image")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class)
                .consumeWith(result -> {
                    byte[] responseBody = result.getResponseBody();
                    assertThat(responseBody).isEqualTo(new byte[0]);
                });
    }
}