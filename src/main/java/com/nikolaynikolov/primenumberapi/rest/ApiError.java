package com.nikolaynikolov.primenumberapi.rest;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiError {
  private final String message;
  private int status;
  private final String reason;
}
