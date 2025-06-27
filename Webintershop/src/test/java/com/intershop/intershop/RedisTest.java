package com.intershop.intershop;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(EmbeddedRedisConfiguration.class)
@EnableCaching
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Disabled
class RedisTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;


    private static final Long PRODUCT_ID = 1L;
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_DESCRIPTION = "Test Description";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("19.99");

    private Product createTestProduct() {
        return new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, null);
    }

    @Test
    void testCacheTtl() throws InterruptedException {
        Product product = createTestProduct();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.getProduct(PRODUCT_ID))
                .expectNext(product)
                .verifyComplete();
        verify(productRepository, times(1)).findById(PRODUCT_ID);

        StepVerifier.create(productService.getProduct(PRODUCT_ID))
                .expectNext(product)
                .verifyComplete();
        verify(productRepository, times(1)).findById(PRODUCT_ID);

        Thread.sleep(3000);

        StepVerifier.create(productService.getProduct(PRODUCT_ID))
                .expectNext(product)
                .verifyComplete();
        verify(productRepository, times(2)).findById(PRODUCT_ID);
    }
    @Test
    void testSaveProduct_ClearsProductListCache() {
        Product product = createTestProduct();
        when(productRepository.save(product)).thenReturn(Mono.just(product));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                anyString(), anyString(), eq(pageable)))
                .thenReturn(Flux.just(product));
        when(productRepository.countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString()))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(productService.getProductsWithPaginationAndSort("", pageable))
                .expectNextMatches(dto -> dto.getTotalElements() == 1)
                .verifyComplete();

        StepVerifier.create(productService.save(product))
                .expectNext(product)
                .verifyComplete();

        StepVerifier.create(productService.getProductsWithPaginationAndSort("", pageable))
                .expectNextMatches(dto -> dto.getTotalElements() == 1)
                .verifyComplete();
    }

}
