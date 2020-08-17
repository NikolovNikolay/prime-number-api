package com.nikolaynikolov.primenumberapi.rest;

import com.nikolaynikolov.primenumberapi.NotSupportedNumberException;
import com.nikolaynikolov.primenumberapi.TooManyRequestsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(NotSupportedNumberException.class)
  protected ResponseEntity<Object> handleBadRequestError(NotSupportedNumberException ex,
                                                         WebRequest request) {
    return buildResponseEntity(ex, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(TooManyRequestsException.class)
  protected ResponseEntity<Object> handleBadRequestError(TooManyRequestsException ex,
                                                         WebRequest request) {
    return buildResponseEntity(ex, HttpStatus.TOO_MANY_REQUESTS, request);
  }

  private ResponseEntity<Object> buildResponseEntity(Exception error, HttpStatus status, WebRequest request) {
    ApiError ae = ApiError.builder().status(status.value()).reason(status.getReasonPhrase()).message(error.getMessage()).build();
    return handleExceptionInternal(error, ae, new HttpHeaders(), status, request);
  }
}
