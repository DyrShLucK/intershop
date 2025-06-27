package com.intershop.intershop.repository;

import com.intershop.intershop.model.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository extends R2dbcRepository<Cart, Integer> {
    @Query("SELECT * FROM cart ORDER BY id DESC")
    Flux<Cart> findAllByCartByIdDesc();
    Mono<Cart> findByUserName(String userName);
}
