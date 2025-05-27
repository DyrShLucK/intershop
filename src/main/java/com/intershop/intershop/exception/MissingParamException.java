package com.intershop.intershop.exception;

public class MissingParamException extends RuntimeException{
    public MissingParamException(String param) {
        super("Отсутствует необходимый параметр:" + param);
    }
}
