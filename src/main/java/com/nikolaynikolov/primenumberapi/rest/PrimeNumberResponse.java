package com.nikolaynikolov.primenumberapi.rest;


import lombok.Builder;
import lombok.Data;

@Data
public class PrimeNumberResponse {

  private boolean isPrime;
  private Integer number;

  public PrimeNumberResponse(boolean isPrime, Integer number) {
    this.isPrime = isPrime;
    this.number = number;
  }

  public PrimeNumberResponse() {
    // Jackson serialization constructor
  }
}
