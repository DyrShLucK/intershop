package com.intershop.intershop.service;

import com.intershop.intershop.api.DefaultApi;
import com.intershop.intershop.model.BalanceResponse;
import com.intershop.intershop.model.PaymentRequest;
import com.intershop.intershop.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class PayService {
    // Без OpenApiGenerator
/*
    private final WebClient webClient;

    public PayService(@Value("${payment.service.url}") String paymentServiceUrl, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(paymentServiceUrl).build();
    }

    public Mono<Float> getBalance() {
        return webClient.get()
                .uri("/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::getBalance);
    }

    public Mono<Boolean> processPayment(double amount) {
        return webClient.post()
                .uri("/payment")
                .bodyValue(new PaymentRequest().amount((float) amount))
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .map(PaymentResponse::getSuccess);
    }*/

    private final DefaultApi defaultApi;
        public PayService(DefaultApi defaultApi) {
            this.defaultApi = defaultApi;
        }
        public Mono<Float> getBalance() {
            return defaultApi.balanceGet()
                    .map(BalanceResponse::getBalance);
        }
        public Mono<Boolean> processPayment(double amount) {
            PaymentRequest request = new PaymentRequest().amount((float) amount);
            return defaultApi.paymentPost(request)
                    .map(PaymentResponse::getSuccess);
        }
}
