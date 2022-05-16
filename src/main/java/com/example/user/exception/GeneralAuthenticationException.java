package com.example.user.exception;

import org.springframework.security.core.AuthenticationException;

public class GeneralAuthenticationException extends AuthenticationException {
    public GeneralAuthenticationException(String msg) {
        super(msg);
    }
}
