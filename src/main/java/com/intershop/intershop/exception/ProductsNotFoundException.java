package com.intershop.intershop.exception;

public class ProductsNotFoundException extends RuntimeException {
  public ProductsNotFoundException(String message) {
    super(message);
  }
}
