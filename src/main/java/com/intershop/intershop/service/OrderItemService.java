package com.intershop.intershop.service;

import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.repository.OrderItemRepository;
import com.intershop.intershop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public OrderItemService(OrderItemRepository orderItemRepository, ProductService productService) {
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
    }

    public Mono<OrderItem> save(OrderItem item) {
        return orderItemRepository.save(item);
    }

    public Flux<OrderItem> saveAll(Flux<OrderItem> items) {
        return orderItemRepository.saveAll(items);
    }

    public Mono<Void> deleteByOrderId(Long orderId) {
        return orderItemRepository.deleteByOrderId(orderId);
    }
    public Flux<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .concatMap(orderItem -> productService.getProduct(orderItem.getProductId())
                        .map(product -> {
                            orderItem.setProduct(product);
                            return orderItem;
                        }));
    }
}
