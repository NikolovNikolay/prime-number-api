package com.nikolaynikolov.primenumberapi.rest;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrimeNumberResponse {
  private boolean isPrime;
  private String number;
}
