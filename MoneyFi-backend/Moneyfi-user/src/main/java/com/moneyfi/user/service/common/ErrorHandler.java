package com.moneyfi.user.service.common;

import com.moneyfi.user.exceptions.CloudinaryImageException;
import com.moneyfi.user.exceptions.CustomInternalServerErrorException;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.service.common.dto.response.ErrorResponse;
import jakarta.ws.rs.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ScenarioNotPossibleException.class, BadRequestException.class})
    public ResponseEntity<ErrorResponse> handleScenarioNotFoundExceptionFunction(ScenarioNotPossibleException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundExceptionFunction(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({CustomInternalServerErrorException.class, CloudinaryImageException.class})
    public ResponseEntity<ErrorResponse> handleHttpServerErrorExceptionFunction(CustomInternalServerErrorException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
