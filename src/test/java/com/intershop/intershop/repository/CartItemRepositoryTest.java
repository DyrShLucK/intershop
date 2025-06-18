package com.intershop.intershop.repository;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DataR2dbcTest(excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class
})
@Disabled
public class CartItemRepositoryTest {
    @MockBean
    private CacheManager cacheManager;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll()
                .then(productRepository.deleteAll())
                .then()
                .block();
    }
    @DirtiesContext
    @Test
    void testFindByProductId() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("A test product");
        product.setPrice(BigDecimal.TEN);
        product.setImage(new byte[0]);

        productRepository.save(product)
                .flatMap(p -> {
                    CartItem item = new CartItem(null, p.getId(), 2);
                    return cartItemRepository.save(item);
                })
                .then()
                .block();

        cartItemRepository.findByProductId(product.getId())
                .as(StepVerifier::create)
                .expectNextMatches(item -> item.getProductId().equals(product.getId()))
                .verifyComplete();
    }
    @DirtiesContext
    @Test
    void testCalculateTotalPrice() {
        Product product1 = createProduct(null, "Product A", "Desc A", BigDecimal.valueOf(10));
        Product product2 = createProduct(null, "Product B", "Desc B", BigDecimal.valueOf(20));

        Flux<Product> savedProducts = productRepository.saveAll(Flux.just(product1, product2));

        List<Product> productList = savedProducts.collectList().block();

        CartItem item1 = new CartItem(null, productList.get(0).getId(), 3);
        CartItem item2 = new CartItem(null, productList.get(1).getId(), 2);

        cartItemRepository.saveAll(Flux.just(item1, item2))
                .then()
                .block();

        Mono<BigDecimal> totalPrice = cartItemRepository.calculateTotalPrice();

        StepVerifier.create(totalPrice)
                .expectNextMatches(price -> price.compareTo(BigDecimal.valueOf(70)) == 0)
                .verifyComplete();
    }
    @DirtiesContext
    @Test
    void testFindAllWithProductSortedById() {
        Product product1 = createProduct(null, "Product B", "Desc B", BigDecimal.valueOf(20));
        Product product2 = createProduct(null, "Product A", "Desc A", BigDecimal.valueOf(10));

        productRepository.saveAll(Flux.just(product1, product2))
                .then()
                .block();

        CartItem item1 = new CartItem(null, 2L, 1);
        CartItem item2 = new CartItem(null, 1L, 2);

        cartItemRepository.saveAll(Flux.just(item1, item2))
                .then()
                .block();

        Flux<CartItem> result = cartItemRepository.findAllWithProductSortedById();

        StepVerifier.create(result)
                .expectNextCount(2)
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