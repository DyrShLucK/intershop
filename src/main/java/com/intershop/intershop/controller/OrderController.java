package com.intershop.intershop.controller;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("intershop/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public Mono<String> getOrder(@PathVariable Long id, Model model) {
        return orderService.findOrderById(id)
                .flatMap(order -> orderService.getOrderItems(id)
                        .collectList()
                        .map(items -> {
                            model.addAttribute("order", order);
                            model.addAttribute("items", items);
                            return "order";
                        }));
    }

    @GetMapping
    public Mono<String> getOrders(Model model) {
        return orderService.findAll()
                .concatMap(order -> orderService.getOrderItems(order.getId())
                        .collectList()
                        .map(items -> Tuples.of(order, items)))
                .collectList()
                .map(orderItems -> {
                    model.addAttribute("orders", orderItems.stream()
                            .map(tuple -> tuple.getT1())
                            .collect(Collectors.toList()));
                    model.addAttribute("orderItems", orderItems.stream()
                            .map(tuple -> tuple.getT2())
                            .collect(Collectors.toList()));
                    return "orders";
                });
    }
}
