package com.payService.controller;
import com.payService.api.DefaultApi;
import com.payService.domain.BalanceResponse;
import com.payService.domain.PaymentRequest;
import com.payService.domain.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicReference;


@RestController
public class PayServiceController implements DefaultApi {
    private final AtomicReference<Float> balance = new AtomicReference<>(10000.0F);

    @Override
    public Mono<ResponseEntity<BalanceResponse>> balanceGet(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new BalanceResponse().balance(balance.get())));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentPost(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(request -> {
            float amount = request.getAmount();
            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().body(new PaymentResponse().success(false)));
            }
            if (balance.get() >= amount) {
                balance.updateAndGet(v -> v - amount);
                return Mono.just(ResponseEntity.ok(new PaymentResponse().success(true)));
            } else {
                return Mono.just(ResponseEntity.ok().body(new PaymentResponse().success(false)));
            }
        });
    }
}
