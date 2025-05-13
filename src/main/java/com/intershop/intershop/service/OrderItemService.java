package com.intershop.intershop.service;

import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.repository.OrderItemRepository;
import com.intershop.intershop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderItemService {

    @Autowired
    OrderItemRepository orderItemRepository;

    public OrderItem save(OrderItem orderItem){
        return orderItemRepository.save(orderItem);
    }

    public void saveAll(Iterable<OrderItem> orderItems){
         orderItemRepository.saveAll(orderItems);
    }
}
