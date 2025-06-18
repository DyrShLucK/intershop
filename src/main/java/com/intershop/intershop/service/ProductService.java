package com.intershop.intershop.service;

import com.intershop.intershop.DTO.ProductPageDTO;
import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.exception.ProductsNotFoundException;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@EnableCaching
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @Cacheable(value = "products", key = "#id")
    public Mono<Product> getProduct(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }
    @Cacheable(value = "productList", key = "#search + ':' + (#pageable?.pageNumber ?: 0) + ':' + (#pageable?.pageSize ?: 10)")
    public Mono<ProductPageDTO> getProductsWithPaginationAndSort(String search, Pageable pageable) {

        Mono<List<Product>> products = productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable)
                .collectList();
        //products.switchIfEmpty(Mono.error(new ProductsNotFoundException()));
        Mono<Long> count = productRepository.countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search);

        return Mono.zip(products, count)
                .map(tuple -> {
                    List<Product> productList = tuple.getT1();
                    long total = tuple.getT2();

                    return new ProductPageDTO(
                            productList,
                            pageable,
                            total,
                            search
                    );
                });
    }
    @CacheEvict(value = "productList", allEntries = true)
    public Mono<Product> save(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
}