package com.intershop.intershop.service;

import com.intershop.intershop.DTO.CartViewModel;
import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.exception.ProductsNotFoundException;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final PayService payService;
    private final CartService cartService;

    public CartItemService(CartItemRepository cartItemRepository, ProductService productService, PayService payService, CartService cartService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.payService = payService;
        this.cartService = cartService;
    }

    public Flux<CartItem> getCart(String username) throws CartEmptyException {
        return cartService.getCartItemsByUserName(username);
    }

    public Mono<Void> deleteAll() {
        return cartItemRepository.deleteAll();
    }

    public Mono<Integer> getQuantityByProductId(Long productId) {
        return cartItemRepository.findByProductId(productId)
                .map(CartItem::getQuantity)
                .defaultIfEmpty(0);
    }

    public Mono<Map<Long, Integer>> getCartQuantitiesMap(Principal principal) {
        if (principal == null) {
            return Mono.just(Collections.emptyMap());
        }
        return getCart(cartService.getUserName(principal))
                .collectList()
                .map(items -> {
                    Map<Long, Integer> quantities = new HashMap<>();
                    if (items != null && !items.isEmpty()) {
                        items.forEach(item -> quantities.put(item.getProductId(), item.getQuantity()));
                    }
                    return quantities;
                });
    }


    public Flux<Product> getProductsInCart() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> productService.getProduct(cartItem.getProductId()))
                .onErrorResume(ProductNotFoundException.class, ex -> Mono.empty()).switchIfEmpty(Flux.empty());
    }

    public Mono<BigDecimal> getTotal() {
        return cartItemRepository.calculateTotalPrice()
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
