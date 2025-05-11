package com.intershop.intershop.repository;

import com.intershop.intershop.model.Order;
import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class OrderRepositoryTest {
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

        OrderItem orderitem2 = new OrderItem();
        orderitem2.setPrice(new BigDecimal("222"));
        orderitem2.setProduct(tablet);
        orderitem2.setQuantity(22);
        orderitem2.setOrder(order1);

        orderItemRepository.save(orderitem1);
        orderItemRepository.save(orderitem2);
    }

    @Test
    void findAllByOrderByIdDesc_ShouldReturnListofOrders(){
        List<Order> orders = orderRepository.findAllByOrderByIdDesc();

        assertThat(orders).hasSize(1);
        Order order = orders.get(0);

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        assertThat(items).hasSize(2);

        assertThat(items).anyMatch(item -> item.getProduct().getName().equals("Laptop") && item.getQuantity() == 2);
        assertThat(items).anyMatch(item -> item.getProduct().getName().equals("Tablet") && item.getQuantity() == 22);
    }
}
