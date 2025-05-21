package com.moneyfi.expense.exceptions;

public class QueryValidationException extends RuntimeException{
    public QueryValidationException(String message){
        super(message);
    }
}
