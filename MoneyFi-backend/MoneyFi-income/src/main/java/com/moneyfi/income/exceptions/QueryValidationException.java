package com.moneyfi.income.exceptions;

public class QueryValidationException extends RuntimeException{
    public QueryValidationException(String message){
        super(message);
    }
}
