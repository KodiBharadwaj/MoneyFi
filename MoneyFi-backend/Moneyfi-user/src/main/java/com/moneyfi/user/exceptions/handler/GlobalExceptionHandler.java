package com.moneyfi.user.exceptions.handler;

import com.moneyfi.user.exceptions.*;
import com.moneyfi.user.service.primary.user.dto.response.ErrorResponse;
import jakarta.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static com.moneyfi.user.util.constants.StringConstants.SOMETHING_WENT_WRONG;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ScenarioNotPossibleException.class, BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleScenarioNotFoundExceptionFunction(Exception exception) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectPassword(IncorrectPasswordException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.getMessage()));
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResultException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundExceptionFunction(Exception exception) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({CustomInternalServerErrorException.class, CloudinaryImageException.class, QueryValidationException.class})
    public ResponseEntity<ErrorResponse> handleHttpServerErrorExceptionFunction(Exception exception) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errors.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> type = ex.getRequiredType();
        String message = (type != null && type.isEnum())
                ? "Invalid value '%s' for '%s'. Allowed values: %s".formatted(
                ex.getValue(), ex.getName(), Arrays.toString(type.getEnumConstants()))
                : "Invalid value '%s' for '%s'".formatted(ex.getValue(), ex.getName());
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Missing required parameter: " + ex.getParameterName()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String field = cv.getPropertyPath().toString();
                    field = field.substring(field.lastIndexOf('.') + 1);
                    return field + ": " + cv.getMessage();
                })
                .toList();
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errors.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception exception) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
