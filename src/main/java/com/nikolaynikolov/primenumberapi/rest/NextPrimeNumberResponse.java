package com.nikolaynikolov.primenumberapi.rest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextPrimeNumberResponse {

  private Long nextPrime;
  private Long number;
}
