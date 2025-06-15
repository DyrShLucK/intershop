package com.payService.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class payServiceControllerTest {
    @Autowired
    WebTestClient webClient;

    @Test
    void testBalanceEndpoint() {
        webClient.get().uri("/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isNumber();
    }

    @Test
    void testPaymentSuccess() {
        webClient.post().uri("/payment")
                .bodyValue(Map.of("amount", 200))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }
}

