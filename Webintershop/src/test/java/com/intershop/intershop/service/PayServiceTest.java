package com.intershop.intershop.service;

import com.intershop.intershop.api.DefaultApi;
import com.intershop.intershop.model.BalanceResponse;
import com.intershop.intershop.model.PaymentRequest;
import com.intershop.intershop.model.PaymentResponse;
import com.intershop.intershop.service.PayService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PayServiceTest {
    /*
    private MockWebServer mockWebServer;
    private PayService payService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        payService = new PayService(WebClient.builder());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetBalanceSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"balance\": 1000.0}")
                .addHeader("Content-Type", "application/json"));

        Mono<Float> balanceMono = payService.getBalance();

        balanceMono.subscribe(balance -> {
            assertThat(balance).isEqualTo(1000.0f);
        });
    }

    @Test
    void testGetBalance404() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> payService.getBalance().block())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testProcessPaymentSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"success\": true}")
                .addHeader("Content-Type", "application/json"));

        Mono<Boolean> result = payService.processPayment(200.0);

        result.subscribe(success -> {
            assertThat(success).isTrue();
        });
    }

    @Test
    void testProcessPaymentError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> payService.processPayment(200.0).block())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testProcessPaymentInvalidResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"success\": \"invalid\"}")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> payService.processPayment(200.0).block())
                .isInstanceOf(RuntimeException.class);
    }*/
    private DefaultApi defaultApi;
    private PayService payService;

    @BeforeEach
    void setUp() {
        defaultApi = mock(DefaultApi.class);
        payService = new PayService(defaultApi);
    }

    @Test
    void testGetBalanceSuccess() {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000.0f);

        when(defaultApi.balanceGet()).thenReturn(Mono.just(balanceResponse));

        Mono<Float> balanceMono = payService.getBalance();

        StepVerifier.create(balanceMono)
                .expectNext(1000.0f)
                .verifyComplete();
    }

    @Test
    void testGetBalanceError() {
        when(defaultApi.balanceGet()).thenReturn(Mono.error(new RuntimeException("API Error")));

        Mono<Float> balanceMono = payService.getBalance();

        StepVerifier.create(balanceMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("API Error"))
                .verify();
    }

    @Test
    void testProcessPaymentSuccess() {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess(true);

        when(defaultApi.paymentPost(any(PaymentRequest.class)))
                .thenReturn(Mono.just(paymentResponse));

        Mono<Boolean> result = payService.processPayment(200.0);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testProcessPaymentError() {
        when(defaultApi.paymentPost(any(PaymentRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Payment failed")));

        Mono<Boolean> result = payService.processPayment(200.0);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Payment failed"))
                .verify();
    }
}