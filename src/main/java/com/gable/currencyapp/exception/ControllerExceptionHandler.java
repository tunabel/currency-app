package com.gable.currencyapp.exception;


import com.gable.currencyapp.dto.app.ErrorMessage;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {

  @ExceptionHandler(value = InvalidRequestException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ErrorMessage invalidRequestException(InvalidRequestException ex, WebRequest webRequest) {
    return new ErrorMessage(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage()
    );
  }

}
