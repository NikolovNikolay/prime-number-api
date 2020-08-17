package com.nikolaynikolov.primenumberapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {
  public TooManyRequestsException() {
    super("You have exceeded your rate limits for the API");
  }

  public TooManyRequestsException(String message) {
    super(message);
  }
}
