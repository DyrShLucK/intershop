package com.intershop.intershop.controller;

import com.intershop.intershop.exception.MissingParamException;
import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
public class GlobalExceptionController {

    @ExceptionHandler(IOException.class)
    public Mono<String> handleIOException(IOException ex, ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> {
                    session.getAttributes().put("error", "Ошибка чтения изображения: " + ex.getMessage());
                })
                .thenReturn("redirect:/admin/add-product");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<String> handleProductNotFound(ProductNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.value());
        return Mono.just("error");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<String> handleOrderNotFound(OrderNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.value());
        return Mono.just("error");
    }
    @ExceptionHandler(MissingParamException.class)
    public Mono<String> handleOrderNotFound(MissingParamException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.value());
        return Mono.just("error");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<String> handleNoResourceFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("message", "Страница не найдена: " + ex.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.value());
        return Mono.just("error");
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public Mono<String> handleMethodNotSupported(MethodNotAllowedException ex, Model model) {
        model.addAttribute("message", "Метод не поддерживается");
        model.addAttribute("statusCode", HttpStatus.METHOD_NOT_ALLOWED.value());
        return Mono.just("error");
    }

    @ExceptionHandler(Exception.class)
    public Mono<String> handleGlobalError(Exception ex, Model model) {
        model.addAttribute("message", "Внутренняя ошибка сервера: " + ex.getMessage());
        model.addAttribute("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Mono.just("error");
    }
}