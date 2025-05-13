package com.intershop.intershop.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Заказ с ID " + orderId + " не найден");
    }
}