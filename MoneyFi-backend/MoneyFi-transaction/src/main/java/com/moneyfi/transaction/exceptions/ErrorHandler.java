package com.moneyfi.transaction.exceptions;

import com.moneyfi.transaction.service.income.dto.response.ErrorResponse;
import com.moneyfi.transaction.service.transaction.dto.response.GmailSyncErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<List<GmailSyncErrorResponse>> handleGenericException(GenericException ex) {
        return ResponseEntity.badRequest().body(ex.getErrorList());
    }
}
