package com.moneyfi.transaction.exceptions;

public class QueryValidationException extends RuntimeException{
    public QueryValidationException(String message){
        super(message);
    }
}
