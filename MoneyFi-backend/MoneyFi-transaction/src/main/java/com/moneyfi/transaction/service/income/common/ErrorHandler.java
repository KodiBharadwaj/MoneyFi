package com.moneyfi.transaction.service.income.common;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.service.income.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleToggleDeactivationException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ScenarioNotPossibleException.class})
    public ResponseEntity<ErrorResponse> handleScenarioNotFoundExceptionFunction(ScenarioNotPossibleException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
