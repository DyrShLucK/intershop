package com.intershop.intershop;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import com.intershop.intershop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Disabled
class RedisCacheTest {
    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private static final Long PRODUCT_ID = 1L;

    @Test
    void testGetProduct_Cacheable() {
        Product product = new Product(PRODUCT_ID, "Test", "Desc", BigDecimal.TEN, null);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.getProduct(PRODUCT_ID))
                .expectNext(product)
                .verifyComplete();

        StepVerifier.create(productService.getProduct(PRODUCT_ID))
                .expectNext(product)
                .verifyComplete();

        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }
}