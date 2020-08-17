package com.nikolaynikolov.primenumberapi.rest;

import com.nikolaynikolov.primenumberapi.NotSupportedNumberException;
import com.nikolaynikolov.primenumberapi.TooManyRequestsException;
import com.nikolaynikolov.primenumberapi.configuration.PrimeCalculationConfig;
import com.nikolaynikolov.primenumberapi.service.CacheService;
import com.nikolaynikolov.primenumberapi.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Slf4j
@RestController
public class PrimeNumberApiController {

  private final CacheService cacheService;
  private final RateLimitService rateLimitService;
  private final PrimeCalculationConfig primeCalculationConfig;

  @Autowired
  public PrimeNumberApiController(CacheService cacheService,
                                  RateLimitService rateLimitService,
                                  PrimeCalculationConfig primeCalculationConfig) {
    this.cacheService = cacheService;
    this.rateLimitService = rateLimitService;
    this.primeCalculationConfig = primeCalculationConfig;
  }

  @RequestMapping(path = "/prime/{number}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON})
  public PrimeNumberResponse checkIfPrimeNumber(@PathVariable String number,
                                                @Context HttpServletRequest request) {
    if (!rateLimitService.canProceed(request.getRemoteAddr())) {
      throw new TooManyRequestsException();
    }

    long num = validateNumber(number);
    return PrimeNumberResponse.builder().isPrime(cacheService.checkIfPrimeNumber(num)).number(num).build();
  }

  @RequestMapping(path = "/prime/next/{number}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON})
  public NextPrimeNumberResponse getNextPrimeNumber(@PathVariable String number,
                                                    @Context HttpServletRequest request) {
    if (!rateLimitService.canProceed(request.getRemoteAddr())) {
      throw new TooManyRequestsException();
    }

    long num = validateNumber(number);
    var nextPrime = cacheService.getNextPrimeNumber(num);
    return NextPrimeNumberResponse.builder().nextPrime(nextPrime).number(num).build();
  }

  private long validateNumber(String number) {

    try {
      long num = Long.parseLong(number);
      if (num < 2 || num > primeCalculationConfig.getMax()) {
        log.info("The passed number is not in the valid bounds");
        throw new NotSupportedNumberException("The provided number is invalid. Please provide a number between 2 and " + primeCalculationConfig.getMax());
      }
      return num;
    } catch (NumberFormatException e) {
      log.error("Passed number is invalid", e);
      throw new NotSupportedNumberException("Invalid number: " + number);
    }
  }
}
