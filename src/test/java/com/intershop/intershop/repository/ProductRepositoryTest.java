package com.intershop.intershop.repository;

import com.intershop.intershop.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class ProductRepositoryTest {

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

        entityManager.persist(laptop);
        entityManager.persist(phone);
        entityManager.persist(tablet);
    }
    @DirtiesContext
    @Test
    void shouldFindProductsByNameIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("lap", null, pageable);
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
    }
    @DirtiesContext
    @Test
    void shouldFindProductsByDescriptionIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(null, "camera", pageable);
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Phone");
    }
    @DirtiesContext
    @Test
    void shouldFindMultipleProductsByPartialMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("tab", "tab", pageable);
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Tablet");
    }
    @DirtiesContext
    @Test
    void shouldReturnEmptyPageWhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("xyz", "xyz", pageable);
        assertThat(result).isEmpty();
    }
    @DirtiesContext
    @Test
    void shouldPaginateResultsCorrectly() {
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        Page<Product> first = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", firstPage);
        Page<Product> second = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", secondPage);

        assertThat(first).hasSize(2);
        assertThat(second).hasSize(1);
    }
    @DirtiesContext
    @Test
    void shouldFindProductById() {
        Product product = new Product();
        product.setName("Headphones");
        product.setPrice(BigDecimal.valueOf(199.99));
        Product saved = productRepository.save(product);

        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Headphones");
    }
    @DirtiesContext
    @Test
    void shouldReturnEmptyOptionalIfIdNotFound() {
        Optional<Product> found = productRepository.findById(999L);
        assertThat(found).isEmpty();
    }
}