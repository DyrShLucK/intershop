package com.intershop.intershop.exception;

public class ProductsNotFoundException extends RuntimeException {
    public ProductsNotFoundException() {
        super("Товары не найдены");
    }
}
