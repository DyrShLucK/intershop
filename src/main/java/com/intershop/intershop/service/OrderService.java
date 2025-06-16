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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemService cartItemService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final PayService payService;

    public OrderService(OrderRepository orderRepository, CartItemService cartItemService, OrderItemService orderItemService, ProductService productService, PayService payService) {
        this.orderRepository = orderRepository;
        this.cartItemService = cartItemService;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.payService = payService;
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

    public Mono<Order> createOrderFromCart() {
        return cartItemService.getCart()
                .collectList()
                .filter(cartItems -> !cartItems.isEmpty())
                .switchIfEmpty(Mono.error(new CartEmptyException()))
                .flatMap(cartItems -> calculateTotalAmount(cartItems)
                        .flatMap(totalAmount -> payService.processPayment(totalAmount.doubleValue())
                                .flatMap(success -> {
                                    if (Boolean.TRUE.equals(success)) {
                                        Order order = new Order();
                                        order.setOrderDate(LocalDateTime.now());
                                        order.setTotalAmount(totalAmount);

                                        return orderRepository.save(order)
                                                .flatMap(savedOrder -> createOrderItems(savedOrder, cartItems)
                                                        .collectList()
                                                        .flatMap(orderItemsList -> orderItemService.saveAll(Flux.fromIterable(orderItemsList))
                                                                .collectList()
                                                                .map(savedItems -> {
                                                                    savedOrder.setOrderItems(savedItems);
                                                                    return savedOrder;
                                                                })
                                                        )
                                                );
                                    } else {
                                        return Mono.error(new RuntimeException());
                                    }
                                })
                        ));
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

}