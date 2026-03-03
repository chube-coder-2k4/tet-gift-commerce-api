package com.tetgift.exception;

public class BadgeNotFoundException extends RuntimeException {
  public BadgeNotFoundException(String message) {
    super(message);
  }
}
