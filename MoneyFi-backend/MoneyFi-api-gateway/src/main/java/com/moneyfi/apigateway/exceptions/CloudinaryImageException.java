package com.moneyfi.apigateway.exceptions;

public class CloudinaryImageException extends RuntimeException {
  public CloudinaryImageException(String message) {
    super(message);
  }
}