package com.intershop.intershop.repository;

import com.intershop.intershop.model.CartItem;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Mono<CartItem> findByProductId(Long productId);

    @Query("SELECT SUM(quantity * (SELECT price FROM products WHERE id = product_id)) FROM cart_items")
    Mono<BigDecimal> calculateTotalPrice();

    @Query("SELECT ci.*, p.* FROM cart_items ci JOIN products p ON ci.product_id = p.id ORDER BY p.id ASC")
    Flux<CartItem> findAllWithProductSortedById();
}