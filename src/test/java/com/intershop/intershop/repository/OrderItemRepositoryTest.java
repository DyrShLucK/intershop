package com.intershop.intershop.repository;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OrderItemRepositoryTest {
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        Product laptop = new Product();
        laptop.setName("Laptop");
        laptop.setDescription("High performance laptop");
        laptop.setPrice(BigDecimal.valueOf(999.99));

        Product phone = new Product();
        phone.setName("Phone");
        phone.setDescription("Smartphone with camera");
        phone.setPrice(BigDecimal.valueOf(699.99));

        Product tablet = new Product();
        tablet.setName("Tablet");
        tablet.setDescription("Android tablet with long battery life");
        tablet.setPrice(BigDecimal.valueOf(499.99));

        productRepository.save(laptop);
        productRepository.save(phone);
        productRepository.save(tablet);
        Order order1 = new Order();
        order1.setOrderDate(LocalDateTime.now());
        order1.setTotalAmount(new BigDecimal(2));

        orderRepository.save(order1);

        OrderItem orderitem1 = new OrderItem();
        orderitem1.setPrice(new BigDecimal("123"));
        orderitem1.setProduct(laptop);
        orderitem1.setQuantity(2);
        orderitem1.setOrder(order1);

        orderItemRepository.save(orderitem1);

        OrderItem orderitem2 = new OrderItem();
        orderitem2.setPrice(new BigDecimal("222"));
        orderitem2.setProduct(tablet);
        orderitem2.setQuantity(22);
        orderitem2.setOrder(order1);

        orderItemRepository.save(orderitem2);

    }

    @Test
    void findByOrder_ShouldReturnListofOrderItems(){
        Order order1 = orderRepository.findById(1L).orElseThrow();

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order1);

        assertThat(orderItems).hasSize(2);
        assertThat(orderItems).anyMatch(orderItem -> orderItem.getProduct().getName().equals("Laptop"));
    }
}
