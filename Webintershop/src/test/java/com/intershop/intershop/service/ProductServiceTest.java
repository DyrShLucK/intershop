package com.intershop.intershop.service;

import com.intershop.intershop.DTO.ProductPageDTO;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
    }

    @Test
    void getProduct_ShouldReturnProduct_WhenExists() {
        when(productRepository.findById(1L)).thenReturn(Mono.just(testProduct));

        Mono<Product> result = productService.getProduct(1L);

        StepVerifier.create(result)
                .expectNextMatches(product -> product.getId().equals(1L))
                .verifyComplete();

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProduct_ShouldThrowException_WhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Product> result = productService.getProduct(1L);

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getProductsWithPaginationAndSort_ShouldReturnPageWithResults() {
        Pageable pageable = mock(Pageable.class);
        List<Product> products = List.of(testProduct);

        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", pageable))
                .thenReturn(Flux.fromIterable(products));
        when(productRepository.countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test"))
                .thenReturn(Mono.just(1L));

        Mono<ProductPageDTO> result = productService.getProductsWithPaginationAndSort("test", pageable);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getTotalElements() == 1 && dto.getSearch().equals("test"))
                .verifyComplete();
    }


    @Test
    void save_ShouldReturnSavedProduct() {
        when(productRepository.save(testProduct)).thenReturn(Mono.just(testProduct));

        Mono<Product> result = productService.save(testProduct);

        StepVerifier.create(result)
                .expectNext(testProduct)
                .verifyComplete();

        verify(productRepository, times(1)).save(testProduct);
    }

}