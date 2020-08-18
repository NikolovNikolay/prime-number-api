package com.nikolaynikolov.primenumberapi.rest;

import lombok.Data;

@Data
public class NextPrimeNumberResponse {

  private Integer nextPrime;
  private Integer number;

  public NextPrimeNumberResponse(Integer nextPrime, Integer number) {
    this.nextPrime = nextPrime;
    this.number = number;
  }

  public NextPrimeNumberResponse() {
    // jackson serialization constructor
  }
}
