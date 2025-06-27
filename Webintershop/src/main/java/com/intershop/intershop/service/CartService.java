package com.intershop.intershop.service;

import com.intershop.intershop.DTO.CartViewModel;
import com.intershop.intershop.exception.CartNotFoundException;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Cart;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import com.intershop.intershop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final PayService payService;

    public Mono<Cart> getCartByUserName(String username) {
        return cartRepository.findByUserName(username)
                .switchIfEmpty(Mono.defer(() -> createNewCartForUser(username)));
    }

    private Mono<Cart> createNewCartForUser(String username) {
        Cart newCart = new Cart();
        newCart.setUserName(username);
        newCart.setTotalAmount(BigDecimal.ZERO);
        return cartRepository.save(newCart);
    }

    public Flux<CartItem> getCartItemsByUserName(String username) {
        return getCartByUserName(username)
                .flatMapMany(cart -> cartItemRepository.findByCartId(cart.getId()));
    }

    public Mono<Void> clearCartByUserName(String username) {
        return getCartByUserName(username)
                .flatMap(cart -> cartItemRepository.deleteByCartId(cart.getId())
                        .then(updateCartTotalAmount(username))
                );
    }
    public Mono<Void> updateCart(Long productId, String action, Principal principal) {
        String username = getUserName(principal);
        return getCartByUserName(username)
                .flatMap(cart -> productService.getProduct(productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .flatMap(product -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                                .defaultIfEmpty(new CartItem(null,  productId,  0, cart.getId()))
                                .flatMap(cartItem -> {
                                    int newQuantity = "plus".equals(action)
                                            ? cartItem.getQuantity() + 1
                                            : Math.max(0, cartItem.getQuantity() - 1);

                                    if (newQuantity == 0 && cartItem.getId() != null) {
                                        return cartItemRepository.deleteById(cartItem.getId()).then();
                                    } else if (newQuantity > 0) {
                                        cartItem.setQuantity(newQuantity);
                                        return cartItemRepository.save(cartItem).then();
                                    } else {
                                        return Mono.empty().then();
                                    }
                                })
                        )
                )
                .then(updateCartTotalAmount(username));
    }
    public Mono<BigDecimal> getTotalAmount(String username) {
        return cartRepository.findByUserName(username).map(Cart::getTotalAmount);
    }

    public Mono<Void> updateCartTotalAmount(String username) {
        return cartRepository.findByUserName(username)
                .switchIfEmpty(Mono.error(new CartNotFoundException()))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItem -> productService.getProduct(cartItem.getProductId())
                                .switchIfEmpty(Mono.error(new ProductNotFoundException(cartItem.getProductId())))
                                .map(product -> product.getPrice()
                                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                                )
                        )
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .flatMap(total -> {
                            cart.setTotalAmount(total);
                            return cartRepository.save(cart).then();
                        })
                );
    }
    public Mono<CartViewModel> getCartViewModel(Principal principal) {
        String username = getUserName(principal);
        return getCartItemsByUserName(username)
                .collectList()
                .flatMap(items -> {
                    Flux<Product> productsFlux = Flux.fromIterable(items)
                            .flatMap(cartItem -> productService.getProduct(cartItem.getProductId())
                                    .onErrorResume(ProductNotFoundException.class, ex -> Mono.empty()));
                    Map<Long, Integer> quantities = items.stream()
                            .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

                    Mono<List<Product>> productsMono = productsFlux.collectList();
                    Mono<BigDecimal> totalMono = getTotalAmount(username);
                    Mono<Float> balanceMono = payService.getBalance()
                            .onErrorResume(ex -> Mono.just(-1.0f));

                    return Mono.zip(productsMono, totalMono, balanceMono)
                            .map(tuple -> {
                                List<Product> products = tuple.getT1();
                                BigDecimal total = tuple.getT2();
                                Float balance = tuple.getT3();

                                boolean hasSufficientBalance = total != null && total.floatValue() <= balance;
                                boolean paymentServiceAvailable = balance != -1.0f;

                                return new CartViewModel(
                                        products,
                                        quantities,
                                        total,
                                        balance,
                                        hasSufficientBalance,
                                        paymentServiceAvailable
                                );
                            });
                });
    }
    public String getUserName(Principal principal) {
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;

            if (auth instanceof UsernamePasswordAuthenticationToken) {
                return "DB_" + auth.getName();
            } else if (auth instanceof OAuth2AuthenticationToken) {
                return "KC_" + auth.getName();
            }
        }

        return principal.getName();
    }
}