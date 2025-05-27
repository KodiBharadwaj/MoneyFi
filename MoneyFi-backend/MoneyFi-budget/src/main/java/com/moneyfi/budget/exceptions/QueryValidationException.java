package com.moneyfi.budget.exceptions;

public class QueryValidationException extends RuntimeException{
    public QueryValidationException(String message){
        super(message);
    }
}
