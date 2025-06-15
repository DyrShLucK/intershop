package com.intershop.intershop.service;

import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class payServise {
    private final WebClient webClient;

    public PaymentClient(@Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public Mono<BalanceResponse> getBalance() {
        return webClient.get()
                .uri("/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class);
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return webClient.post()
                .uri("/payment")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class);
    }
}
