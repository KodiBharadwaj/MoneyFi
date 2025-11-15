package com.moneyfi.user.exceptions;

public class QueryValidationException extends RuntimeException {
    public QueryValidationException(String message) {
        super(message);
    }
}
