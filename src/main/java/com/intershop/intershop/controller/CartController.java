package com.intershop.intershop.controller;

import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
@RequestMapping("intershop/cart")
public class CartController {
    private final CartItemService cartItemService;
    private final OrderService orderService;

    public CartController(CartItemService cartItemService, OrderService orderService) {
        this.cartItemService = cartItemService;
        this.orderService = orderService;
    }

    @GetMapping
    public Mono<String> getCart(Model model) {
        return cartItemService.getProductsInCart()
                .collectList()
                .flatMap(products -> cartItemService.getCartQuantitiesMap()
                        .flatMap(quantities -> cartItemService.getTotal()
                                .map(total -> {
                                    model.addAttribute("items", products);
                                    model.addAttribute("productQuantities", quantities);
                                    model.addAttribute("total", total != null ? total : BigDecimal.ZERO);
                                    return "cart";
                                })));
    }

    @PostMapping("/{productId}")
    public Mono<String> updateCartItem(
            @PathVariable Long productId,
            ServerWebExchange exchange
    ) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    String redirectUrl = formData.getFirst("redirectUrl");

                    return cartItemService.updateCartItem(productId, action).thenReturn("redirect:" + (redirectUrl != null ? redirectUrl : "/intershop"));});

    }

    @PostMapping("/buy")
    public Mono<String> createOrder(ServerWebExchange exchange) {
        return orderService.createOrderFromCart()
                .doOnSuccess(order -> {
                    exchange.getSession()
                            .flatMap(session -> {
                                session.getAttributes().put("success", "Заказ успешно создан");
                                return Mono.empty();
                            });
                })
                .map(order -> "redirect:/intershop/orders/" + order.getId())
                .onErrorResume(CartEmptyException.class, ex ->
                        Mono.just("redirect:/intershop/cart"));
    }
}
