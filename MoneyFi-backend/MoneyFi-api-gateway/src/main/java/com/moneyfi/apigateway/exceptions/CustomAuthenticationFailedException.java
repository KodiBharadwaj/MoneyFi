package com.moneyfi.apigateway.exceptions;

public class CustomAuthenticationFailedException extends RuntimeException {
    public CustomAuthenticationFailedException(String message) {
        super(message);
    }
}
