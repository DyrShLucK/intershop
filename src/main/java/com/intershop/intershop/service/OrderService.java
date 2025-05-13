package com.intershop.intershop.service;

import com.intershop.intershop.exception.CartEmptyException;
import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.repository.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CartItemService cartItemService;
    @Autowired
    OrderItemService orderItemService;

    public Order save(Order order){
        return orderRepository.save(order);
    }

    public List<Order> findAll(){
        return orderRepository.findAllByOrderByIdDesc();
    }
    public Order findOrderById(Long id){
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
    public void deleteOrder(Long id){
        orderRepository.deleteById(id);
    }

    @Transactional
    public Order createOrderFromCart() {
        List<CartItem> cartItems = cartItemService.getCart();

        if (cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> {
                    BigDecimal productPrice = item.getProduct().getPrice();
                    int quantity = item.getQuantity();

                    return productPrice.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getProduct().getPrice());
                    orderItem.setOrder(savedOrder);
                    return orderItem;
                })
                .collect(Collectors.toList());

        orderItemService.saveAll(orderItems);
        cartItemService.deleteAll();

        return savedOrder;
    }
}
