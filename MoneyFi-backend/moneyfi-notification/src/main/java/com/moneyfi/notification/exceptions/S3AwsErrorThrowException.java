package com.moneyfi.notification.exceptions;

public class S3AwsErrorThrowException extends RuntimeException {
    public S3AwsErrorThrowException(String message) {
        super(message);
    }
}
