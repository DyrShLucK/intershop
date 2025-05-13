package com.intershop.intershop.service;

import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
    }

    @Test
    void getProduct_ExistingId_ShouldReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProduct(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProduct_NonExistingId_ThrowsProductNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(1L));
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductsWithPaginationAndSort_ShouldCallsRepositoryWithCorrectParams() {
        Page<Product> page = new PageImpl<>(Arrays.asList(testProduct));
        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(0, 10, sort);

        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "desc", pageable))
                .thenReturn(page);

        Page<Product> result = productService.getProductsWithPaginationAndSort("test", "desc", 0, 10, "name", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "desc", pageable);
    }

    @Test
    void save_DelegatesToRepository() {
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        Product result = productService.save(testProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void deleteProduct_ShouldCallsDeleteById() {
        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}