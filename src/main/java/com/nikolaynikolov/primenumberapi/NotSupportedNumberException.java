package com.nikolaynikolov.primenumberapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotSupportedNumberException extends RuntimeException {
  public NotSupportedNumberException() {
    super();
  }

  public NotSupportedNumberException(String message) {
    super(message);
  }
}
