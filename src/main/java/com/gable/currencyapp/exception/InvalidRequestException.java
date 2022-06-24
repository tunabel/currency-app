package com.gable.currencyapp.exception;

public class InvalidRequestException extends Exception {

  public InvalidRequestException() {
    super();
  }

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
