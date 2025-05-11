package com.intershop.intershop.repository;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

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

        CartItem cartItem1 = new CartItem();
        cartItem1.setProduct(laptop);
        cartItem1.setQuantity(2);

        CartItem cartItem2 = new CartItem();
        cartItem2.setProduct(tablet);
        cartItem2.setQuantity(4);

        cartItemRepository.save(cartItem1);
        cartItemRepository.save(cartItem2);
    }

    @Test
    void ShouldReturnCart(){
        List<CartItem> cart = cartItemRepository.findAll();

        assertThat(cart).hasSize(2);
        assertThat(cart).anyMatch(cartItem -> cartItem.getProduct().getName().equals("Tablet") && cartItem.getQuantity() == 4 );
    }

}