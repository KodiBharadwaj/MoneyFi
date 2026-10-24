package com.moneyfi.user.exceptions;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }
    public FileStorageException(String message, Throwable error) {
        super(message, error);
    }
}
