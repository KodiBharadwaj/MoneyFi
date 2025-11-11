package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.exceptions.CloudinaryImageException;
import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.service.common.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(413, "File size exceeds allowed limit"),
                HttpStatus.PAYLOAD_TOO_LARGE
        );
    }

    @ExceptionHandler(CloudinaryImageException.class)
    public ResponseEntity<ErrorResponse> handleCloudinary(CloudinaryImageException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(500, ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(QueryValidationException.class)
    public ResponseEntity<ErrorResponse> handleQueryValidation(QueryValidationException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(500, ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse(500, "Internal server error"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}