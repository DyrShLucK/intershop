package com.intershop.intershop.controller;

import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.CartService;
import com.intershop.intershop.service.OrderService;
import com.intershop.intershop.service.PayService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("intershop/cart")
public class CartController {
    private final CartItemService cartItemService;
    private final OrderService orderService;
    private final PayService payService;
    private final CartService cartService;

    public CartController(CartItemService cartItemService, OrderService orderService, PayService payService, CartService cartService) {
        this.cartItemService = cartItemService;
        this.orderService = orderService;
        this.payService = payService;
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> getCart(Model model,
                                Principal principal) {
        return cartService.getCartViewModel(principal)
                .doOnNext(viewModel -> {
                    model.addAttribute("items", viewModel.getItems());
                    model.addAttribute("productQuantities", viewModel.getProductQuantities());
                    model.addAttribute("total", viewModel.getTotal() != null ? viewModel.getTotal() : BigDecimal.ZERO);
                    model.addAttribute("balance", viewModel.getBalance());
                    model.addAttribute("hasSufficientBalance", viewModel.isHasSufficientBalance());
                    model.addAttribute("paymentServiceAvailable", viewModel.isPaymentServiceAvailable());
                })
                .thenReturn("cart");
    }

    @PostMapping("/{productId}")
    public Mono<String> updateCartItem(
            @PathVariable Long productId,
            ServerWebExchange exchange,
            Principal principal
    ) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    String redirectUrl = formData.getFirst("redirectUrl");

                    return cartService.updateCart(productId, action, principal)
                            .thenReturn("redirect:" + (redirectUrl != null ? redirectUrl : "/intershop"));
                });
    }

    @PostMapping("/buy")
    public Mono<String> createOrder(ServerWebExchange exchange,
                                    Principal principal) {
        return orderService.createOrderFromCart(principal)
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
