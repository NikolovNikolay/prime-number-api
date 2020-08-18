package com.nikolaynikolov.primenumberapi.rest;

import com.nikolaynikolov.primenumberapi.NotSupportedNumberException;
import com.nikolaynikolov.primenumberapi.TooManyRequestsException;
import com.nikolaynikolov.primenumberapi.configuration.PrimeCalculationConfig;
import com.nikolaynikolov.primenumberapi.service.CacheService;
import com.nikolaynikolov.primenumberapi.service.RateLimitService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("primes/api/v1")
@Api(value = "prime-number-api")
public class PrimeNumberApiController {

  private static final String NUMBER_INPUT_COUNTER = "number_input_counter";
  private static final String NUMBER_INPUT_TAG = "number_input";
  private static final String REQUEST_COUNTER = "request_counter";
  private static final String ENDPOINT_NAME_TAG = "endpoint_name";
  private static final String ENDPOINT_NAME_IF_PRIME_VALUE = "checkIfPrimeNumber";
  private static final String ENDPOINT_NAME_NEXT_PRIME_VALUE = "nextPrimeNumber";
  private static final String STATUS_CODE_TAG = "status_code";
  private static final String USER_KEY_TAG = "user_key";

  private final CacheService cacheService;
  private final RateLimitService rateLimitService;
  private final PrimeCalculationConfig primeCalculationConfig;
  private final MeterRegistry meterRegistry;

  @Autowired
  public PrimeNumberApiController(CacheService cacheService,
                                  RateLimitService rateLimitService,
                                  PrimeCalculationConfig primeCalculationConfig,
                                  MeterRegistry meterRegistry) {
    this.cacheService = cacheService;
    this.rateLimitService = rateLimitService;
    this.primeCalculationConfig = primeCalculationConfig;
    this.meterRegistry = meterRegistry;
  }

  @ApiOperation(
      value = "Endpoint will check if a given number is a prime number in range between 2 and 10 000 000",
      response = PrimeNumberResponse.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Check for prime number was successful"),
      @ApiResponse(code = 400, message = "The provided number string contains invalid characters or is not in range between 2 " +
          "and 10 000 000"),
      @ApiResponse(code = 429, message = "Rate limits were exceeded")
  })
  @RequestMapping(path = "/{number}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON})
  public PrimeNumberResponse checkIfPrimeNumber(@PathVariable String number,
                                                @Context HttpServletRequest request) throws ExecutionException, InterruptedException {
    String userKey = getUserKey(request);
    try {
      applyRateLimit(userKey);
      int num = validateNumber(number);
      meterRegistry.counter(NUMBER_INPUT_COUNTER, NUMBER_INPUT_TAG, String.valueOf(num), ENDPOINT_NAME_TAG, ENDPOINT_NAME_IF_PRIME_VALUE);
      sendRequestMetric(userKey, "200", ENDPOINT_NAME_IF_PRIME_VALUE);
      return new PrimeNumberResponse(cacheService.checkIfPrimeNumber(num), num);
    } catch (NotSupportedNumberException e) {
      sendRequestMetric(userKey, "400", ENDPOINT_NAME_IF_PRIME_VALUE);
      throw e;
    } catch (TooManyRequestsException e) {
      sendRequestMetric(userKey, "429", ENDPOINT_NAME_IF_PRIME_VALUE);
      throw e;
    }
  }

  @ApiOperation(
      value = "Endpoint will return next prime number in range between 2 and 10 000 000",
      response = PrimeNumberResponse.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Check for next prime number was successful"),
      @ApiResponse(code = 400,
          message = "The provided number string contains invalid characters or is not in range between 2 and 10 000 000"),
      @ApiResponse(code = 429, message = "Rate limits were exceeded")
  })
  @RequestMapping(path = "/next/{number}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON})
  public NextPrimeNumberResponse getNextPrimeNumber(@PathVariable String number,
                                                    @Context HttpServletRequest request) throws ExecutionException, InterruptedException {

    String userKey = getUserKey(request);
    try {
      applyRateLimit(userKey);
      int num = validateNumber(number);
      var nextPrime = cacheService.getNextPrimeNumber(num);
      meterRegistry.counter(NUMBER_INPUT_COUNTER, NUMBER_INPUT_TAG, String.valueOf(num), ENDPOINT_NAME_TAG, ENDPOINT_NAME_NEXT_PRIME_VALUE);
      sendRequestMetric(userKey, "200", ENDPOINT_NAME_NEXT_PRIME_VALUE);
      return new NextPrimeNumberResponse(nextPrime, num);
    } catch (TooManyRequestsException e) {
      sendRequestMetric(userKey, "429", ENDPOINT_NAME_NEXT_PRIME_VALUE);
      throw e;
    } catch (NotSupportedNumberException e) {
      sendRequestMetric(userKey, "400", ENDPOINT_NAME_NEXT_PRIME_VALUE);
      throw e;
    }
  }

  private int validateNumber(String number) {

    try {
      int num = Integer.parseInt(number);
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

  private String applyRateLimit(String key) {
    if (!rateLimitService.canProceed(key)) {
      throw new TooManyRequestsException();
    }
    return key;
  }

  private String getUserKey(@Context HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    return !StringUtils.isEmpty(ipAddress) ? ipAddress : request.getRemoteAddr();
  }

  private void sendRequestMetric(String userKey, String responseStatus, String endpointNameIfPrimeValue) {
    meterRegistry.counter(REQUEST_COUNTER, ENDPOINT_NAME_TAG, endpointNameIfPrimeValue,
        STATUS_CODE_TAG, responseStatus, USER_KEY_TAG, userKey).increment();
  }
}
