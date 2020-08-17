package com.nikolaynikolov.primenumberapi.rest;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.math.BigInteger;

@RestController
public class PrimeNumberApiController {

  private final RateLimiter rateLimiter;

  @Autowired
  public PrimeNumberApiController() {
    this.rateLimiter = RateLimiter.create(10);
  }

  @GetMapping(path = "/prime", produces = {MediaType.APPLICATION_JSON})
  public PrimeNumberResponse checkIfPrimeNumber(@RequestParam String number) {

    rateLimiter.acquire();
    BigInteger parsedNumber = new BigInteger(number);
    boolean isPrime = true;
    BigInteger i = BigInteger.TWO;

    while (i.multiply(i).compareTo(parsedNumber) <= 0) {
      if (parsedNumber.mod(i).equals(BigInteger.ZERO)) {
        isPrime = false;
        break;
      }
      i = i.add(BigInteger.ONE);
    }
    return PrimeNumberResponse.builder().isPrime(isPrime).number(number).build();
  }
}
