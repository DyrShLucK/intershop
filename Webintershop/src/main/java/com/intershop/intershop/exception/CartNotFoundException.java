package com.intershop.intershop.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException() {
        super("Корзина пользователя не найдена");
    }
}
