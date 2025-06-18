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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final PayService payService;

    public CartItemService(CartItemRepository cartItemRepository, ProductService productService, PayService payService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.payService = payService;
    }

    public Flux<CartItem> getCart() {
        return cartItemRepository.findAll();
    }

    public Mono<Void> deleteAll() {
        return cartItemRepository.deleteAll();
    }

    public Mono<Integer> getQuantityByProductId(Long productId) {
        return cartItemRepository.findByProductId(productId)
                .map(CartItem::getQuantity)
                .defaultIfEmpty(0);
    }

    public Mono<Map<Long, Integer>> getCartQuantitiesMap() {
        return getCart()
                .collectList()
                .map(items -> {
                    Map<Long, Integer> quantities = new HashMap<>();
                    if (items != null && !items.isEmpty()) {
                        items.forEach(item -> quantities.put(item.getProductId(), item.getQuantity()));
                    }
                    return quantities;
                });
    }

    public Mono<Void> updateCartItem(Long productId, String action) {
        return productService.getProduct(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(product -> cartItemRepository.findByProductId(productId)
                        .defaultIfEmpty(new CartItem(null, productId, 0))
                        .flatMap(cartItem -> {
                            int newQuantity = "plus".equals(action)
                                    ? cartItem.getQuantity() + 1
                                    : Math.max(0, cartItem.getQuantity() - 1);

                            if (newQuantity == 0 && cartItem.getId() != null) {
                                return cartItemRepository.deleteById(cartItem.getId());
                            } else {
                                cartItem.setQuantity(newQuantity);
                                return cartItemRepository.save(cartItem).then();
                            }
                        }));
    }

    public Flux<Product> getProductsInCart() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> productService.getProduct(cartItem.getProductId()))
                .onErrorResume(ProductNotFoundException.class, ex -> Mono.empty()).switchIfEmpty(Flux.empty());
    }
    public Mono<CartViewModel> getCartViewModel() {
        return getProductsInCart()
                .collectList()
                .flatMap(products -> getCartQuantitiesMap()
                        .flatMap(quantities -> getTotal()
                                .flatMap(total -> payService.getBalance()
                                        .onErrorResume(ex -> Mono.just(-1.0f))
                                        .map(balance -> {
                                            boolean hasSufficientBalance = total != null && total.floatValue() <= balance;
                                            return new CartViewModel(
                                                    products,
                                                    quantities,
                                                    total,
                                                    balance,
                                                    hasSufficientBalance,
                                                    balance != -1.0f
                                            );
                                        }))));
    }
    public Mono<BigDecimal> getTotal() {
        return cartItemRepository.calculateTotalPrice()
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
