package com.intershop.intershop.controller;

import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(ProductNotFoundException.class)
    public ModelAndView handleProductNotFound(ProductNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ModelAndView handleOrderNotFound(OrderNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Страница не найдена: " + ex.getRequestURL());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoHandlerFound(NoResourceFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Страница не найдена: " + ex.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @ExceptionHandler({org.springframework.web.HttpRequestMethodNotSupportedException.class})
    public ModelAndView handleMethodNotSupported() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Метод не поддерживается");
        modelAndView.addObject("statusCode", HttpStatus.METHOD_NOT_ALLOWED.value());
        modelAndView.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        return modelAndView;
    }

    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ModelAndView handleMissingParams() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Отсутствует обязательный параметр запроса");
        modelAndView.addObject("statusCode", HttpStatus.BAD_REQUEST.value());
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGlobalError(Exception ex) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Внутренняя ошибка сервера: " + ex.getMessage());
        modelAndView.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return modelAndView;
    }
}