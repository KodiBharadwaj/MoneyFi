package com.moneyfi.apigateway.exceptions;

public class QueryValidationException extends RuntimeException{
    public QueryValidationException(String message){
        super(message);
    }
}
