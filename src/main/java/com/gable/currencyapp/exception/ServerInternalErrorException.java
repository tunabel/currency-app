package com.gable.currencyapp.exception;

public class ServerInternalErrorException extends RuntimeException {

  public ServerInternalErrorException() {
    super();
  }

  public ServerInternalErrorException(String message) {
    super(message);
  }

  public ServerInternalErrorException(String message, Throwable cause) {
    super(message, cause);
  }
}
