package com.intershop.intershop.repository;

import com.intershop.intershop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DataR2dbcTest(excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class
})
@Disabled
public class ProductRepositoryTest {
    @MockBean
    private CacheManager cacheManager;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll()
                .then()
                .block();
    }
    @DirtiesContext
    @Test
    void testFindProductsByNameOrDescriptionIgnoreCase() {
        Product product1 = createProduct(null, "TestProduct", "A product for testing", BigDecimal.TEN);
        Product product2 = createProduct(null, "AnotherItem", "This is a test description", BigDecimal.valueOf(20));
        Product product3 = createProduct(null, "NotRelated", "Some other description", BigDecimal.valueOf(30));
        Product product4 = createProduct(null, "TestingStuff", "Another test", BigDecimal.valueOf(40));

        productRepository.saveAll(Flux.just(product1, product2, product3, product4))
                .then()
                .block();

        Flux<Product> result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", Pageable.unpaged());

        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
    }
    @DirtiesContext
    @Test
    void testCountProductsByNameOrDescriptionIgnoreCase() {
        Product product1 = createProduct(null, "TestProduct", "Desc", BigDecimal.TEN);
        Product product2 = createProduct(null, "Demo", "TestDescription", BigDecimal.valueOf(20));
        Product product3 = createProduct(null, "NoMatch", "Nothing", BigDecimal.valueOf(30));

        productRepository.saveAll(Flux.just(product1, product2, product3))
                .then()
                .block();

        Mono<Long> count = productRepository.countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test");

        StepVerifier.create(count)
                .expectNext(2L)
                .verifyComplete();
    }
    @DirtiesContext
    @Test
    void testPaginationWorksCorrectly() {
        Product product1 = createProduct(null, "TestA", "DescA", BigDecimal.TEN);
        Product product2 = createProduct(null, "TestB", "DescB", BigDecimal.valueOf(20));
        Product product3 = createProduct(null, "TestC", "DescC", BigDecimal.valueOf(30));

        productRepository.saveAll(Flux.just(product1, product2, product3))
                .then()
                .block();

        Flux<Product> firstPage = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", Pageable.ofSize(2));
        Flux<Product> secondPage = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", Pageable.ofSize(2).withPage(1));

        StepVerifier.create(firstPage)
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(secondPage)
                .expectNextCount(1)
                .verifyComplete();
    }

    private Product createProduct(Long id, String name, String description, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(new byte[0]);
        return product;
    }
}