package com.intershop.intershop.repository;

import com.intershop.intershop.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Flux<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String search, String description, Pageable pageable
    );

    @Query("SELECT COUNT(*) FROM products p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Mono<Long> countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String search);
}