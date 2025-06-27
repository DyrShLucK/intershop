package com.intershop.intershop.repository;

import com.intershop.intershop.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface OrderRepository extends R2dbcRepository<Order, Long> {
    @Query("SELECT * FROM orders ORDER BY id DESC")
    Flux<Order> findAllByOrderByIdDesc();
    Flux<Order> findByUserName(String username);
}