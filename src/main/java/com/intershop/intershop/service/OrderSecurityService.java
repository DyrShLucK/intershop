package com.intershop.intershop.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
public class OrderSecurityService {
    private final OrderService orderService;

    public OrderSecurityService(OrderService orderService) {
        this.orderService = orderService;
    }

    public Mono<Boolean> hasOrderAccess(Long orderId, Authentication authentication) {
        return orderService.findOrderById(orderId)
                .flatMap(order -> {
                    boolean isManager = authentication.getAuthorities().stream()
                            .anyMatch(auth -> "ROLE_MANAGER".equals(auth.getAuthority()));
                    String name = "";
                    if (authentication instanceof UsernamePasswordAuthenticationToken) {
                        name = "DB_" + authentication.getName();
                    } else if (authentication instanceof OAuth2AuthenticationToken) {
                        name = "KC_" + authentication.getName();
                    }
                    boolean isOwner = order.getUserName().equals(name);


                    return Mono.just(isManager || isOwner);
                })
                .switchIfEmpty(Mono.just(false));
    }
}
