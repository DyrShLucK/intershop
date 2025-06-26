package com.intershop.intershop.service;


import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.exception.ProductsNotFoundException;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.repository.OrderRepository;

import com.intershop.intershop.repository.ProductRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final PayService payService;
    private final CartService cartService;
    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService, ProductService productService, PayService payService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.payService = payService;
        this.cartService = cartService;
    }

    public Mono<Order> save(Order order) {
        return orderRepository.save(order);
    }

    public Flux<Order> findAll() {
        return orderRepository.findAllByOrderByIdDesc()
                .concatMap(order -> orderItemService.getOrderItemsByOrderId(order.getId())
                        .collectList()
                        .map(items -> {
                            if (items == null || items.isEmpty()) {
                                throw new ProductsNotFoundException();
                            }
                            order.setOrderItems(items);
                            return order;
                        }));
    }

    public Mono<Order> findOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> orderItemService.getOrderItemsByOrderId(order.getId())
                        .collectList()
                        .map(items -> {
                            if (items == null || items.isEmpty()) {
                                throw new ProductsNotFoundException();
                            }
                            order.setOrderItems(items);
                            return order;
                        }));
    }

    public Mono<Void> deleteOrder(Long id) {
        return orderRepository.deleteById(id);
    }

    public Flux<Order> findOrdersByUserName(String username) {
        return orderRepository.findByUserName(username)
                .concatMap(order -> orderItemService.getOrderItemsByOrderId(order.getId())
                        .collectList()
                        .handle((items, sink) -> {
                            if (items == null || items.isEmpty()) {
                                sink.error(new ProductsNotFoundException());
                                return;
                            }
                            order.setOrderItems(items);
                            sink.next(order);
                        }));
    }



    public Mono<Order> createOrderFromCart(Principal principal) {
        String username = getUserName(principal);
        return cartService.getCartByUserName(username)
                .flatMap(cart -> cartService.getTotalAmount(username)
                        .flatMap(totalAmount -> payService.processPayment(totalAmount.doubleValue())
                                .flatMap(success -> {
                                    if (Boolean.TRUE.equals(success)) {
                                        Order order = new Order();
                                        order.setOrderDate(LocalDateTime.now());
                                        order.setTotalAmount(totalAmount);
                                        order.setUserName(username);

                                        return orderRepository.save(order)
                                                .flatMap(savedOrder -> cartService.getCartItemsByUserName(username)
                                                        .concatMap(cartItem -> productService.getProduct(cartItem.getProductId())
                                                                .map(product -> {
                                                                    OrderItem orderItem = new OrderItem();
                                                                    orderItem.setProductId(cartItem.getProductId());
                                                                    orderItem.setOrderId(savedOrder.getId());
                                                                    orderItem.setQuantity(cartItem.getQuantity());
                                                                    orderItem.setPrice(product.getPrice());
                                                                    return orderItem;
                                                                }))
                                                        .collectList()
                                                        .flatMap(orderItems ->
                                                                orderItemService.saveAll(Flux.fromIterable(orderItems))
                                                                        .collectList()
                                                                        .then(cartService.clearCartByUserName(username))
                                                                        .thenReturn(orderItems)
                                                        )
                                                        .map(items -> {
                                                            savedOrder.setOrderItems(items);
                                                            return savedOrder;
                                                        })
                                                );
                                    } else {
                                        return Mono.error(new RuntimeException("Payment failed"));
                                    }
                                })
                        )
                );
    }


    private Mono<BigDecimal> calculateTotalAmount(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(item -> productService.getProduct(item.getProductId())
                        .map(product -> product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Flux<OrderItem> createOrderItems(Order savedOrder, List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .concatMap(cartItem -> productService.getProduct(cartItem.getProductId()).switchIfEmpty(Mono.error(new ProductNotFoundException(cartItem.getProductId())))
                        .map(product -> {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setProductId(cartItem.getProductId());
                            orderItem.setQuantity(cartItem.getQuantity());
                            orderItem.setPrice(product.getPrice());
                            orderItem.setOrderId(savedOrder.getId());
                            return orderItem;
                        }));
    }
    public Flux<OrderItem> getOrderItems(Long orderId) {
        return orderItemService.getOrderItemsByOrderId(orderId);
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