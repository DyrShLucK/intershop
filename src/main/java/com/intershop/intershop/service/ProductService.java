package com.intershop.intershop.service;

import com.intershop.intershop.exception.ProductNotFoundException;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;


    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
    public Page<Product> getProductsWithPaginationAndSort(String name, String description, int pageNumber, int pageSize, String sortBy, String sortDir){
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(name, description, pageable);
    }
    public Product save(Product product){
        return productRepository.save(product);
    }
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }

}
