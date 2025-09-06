package com.moneyfi.apigateway.exceptions;

public class S3AwsErrorThrowException extends RuntimeException {
    public S3AwsErrorThrowException(String message) {
        super(message);
    }
}
