package com.intershop.intershop.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Продукт с ID " + productId + " не найден");
    }
}