package com.intershop.intershop.repository;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);
    Mono<Void> deleteByOrderId(Long OrderId);
}