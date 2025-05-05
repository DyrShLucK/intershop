package com.intershop.intershop.repository;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
