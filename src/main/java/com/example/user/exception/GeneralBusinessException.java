package com.example.user.exception;

public class GeneralBusinessException extends RuntimeException{
    public GeneralBusinessException(String message) {
        super(message);
    }
    public GeneralBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
