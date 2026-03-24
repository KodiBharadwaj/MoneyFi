package com.moneyfi.user.exceptions;

public class CustomAuthenticationFailedException extends RuntimeException {
    public CustomAuthenticationFailedException(String message) {
        super(message);
    }
}
