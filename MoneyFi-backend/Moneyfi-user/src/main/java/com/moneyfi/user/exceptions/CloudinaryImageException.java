package com.moneyfi.user.exceptions;

public class CloudinaryImageException extends RuntimeException {
  public CloudinaryImageException(String message) {
    super(message);
  }
  public CloudinaryImageException(String message, Throwable error) {
    super(message, error);
  }
}