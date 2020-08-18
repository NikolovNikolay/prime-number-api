package com.nikolaynikolov.primenumberapi.rest;

import com.nikolaynikolov.primenumberapi.PrimeapiContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {PrimeapiContextInitializer.class})
class PrimeNumberApiControllerTests {

  @LocalServerPort
  int randomServerPort;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeEach
  public void setUp() {
  }

  @Test
  public void primeApi_shouldBeHealthy() {
    String uri = getBasePath() + "/actuator/health";
    ResponseEntity<Object> result = this.restTemplate.getForEntity(uri, Object.class);

    assertEquals(200, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetSuccessWhenCalling_CheckIfPrimeNumber() {
    int checkNumber = 3; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);

    assertEquals(200, result.getStatusCodeValue());
    assertNotNull(result.getBody());
    assertEquals(checkNumber, result.getBody().getNumber().intValue());
    assertTrue(result.getBody().isPrime());
  }

  @Test
  public void primeApi_shouldGetSuccessWhenCalling_CheckIfPrimeNumber_NotPrime() {
    int checkNumber = 4; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);

    assertEquals(200, result.getStatusCodeValue());
    assertNotNull(result.getBody());
    assertEquals(checkNumber, result.getBody().getNumber().intValue());
    assertFalse(result.getBody().isPrime());
  }

  @Test
  public void primeApi_shouldGetBadResponseWhenCalling_CheckIfPrimeNumber_NumberIsTooLarge() {
    int checkNumber = 50; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    assertEquals(400, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetBadResponseWhenCalling_CheckIfPrimeNumber_InvalidAlphaNumericString() {
    String uri = getBasePath() + "/primes/api/v1/45opdi";
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    assertEquals(400, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetSuccessCalling_GetNextPrimeNumber_givenPrime() {
    int checkNumber = 5;
    String uri = getBasePath() + "/primes/api/v1/next/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<NextPrimeNumberResponse> result = this.restTemplate.getForEntity(uri, NextPrimeNumberResponse.class);

    assertEquals(200, result.getStatusCodeValue());
    assertNotNull(result.getBody());
    assertEquals(checkNumber, result.getBody().getNumber().intValue());
    assertEquals(7, result.getBody().getNextPrime().intValue());
  }

  @Test
  public void primeApi_shouldGetSuccessCalling_GetNextPrimeNumber_givenNotPrime() {
    int checkNumber = 6;
    String uri = getBasePath() + "/primes/api/v1/next/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<NextPrimeNumberResponse> result = this.restTemplate.getForEntity(uri, NextPrimeNumberResponse.class);

    assertEquals(200, result.getStatusCodeValue());
    assertNotNull(result.getBody());
    assertEquals(checkNumber, result.getBody().getNumber().intValue());
    assertEquals(7, result.getBody().getNextPrime().intValue());
  }

  @Test
  public void primeApi_shouldGetBadResponseWhenCalling_GetNextPrimeNumber_InvalidAlphaNumericString() {
    String uri = getBasePath() + "/primes/api/v1/next/45opdi";
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    assertEquals(400, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetBadResponseWhenCalling_GetNextPrimeNumber_NumberIsTooLarge() {
    int checkNumber = 50; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/next/" + checkNumber;
    setRandomIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);

    assertEquals(400, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetTooManyReqWhenCalling_GetNextPrimeNumber() {
    int checkNumber = 3; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/next/" + checkNumber;

    setFixedIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = null;
    for (int i = 0; i < 10; i++) {
      result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    }
    var lastCall = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    assertNotNull(lastCall);
    assertEquals(429, result.getStatusCodeValue());
  }

  @Test
  public void primeApi_shouldGetTooManyReqWhenCalling_CheckIfPrimeNumber() {
    int checkNumber = 3; // Max set in config for the test is 10
    String uri = getBasePath() + "/primes/api/v1/" + checkNumber;

    setFixedIdHeadersToRestTemplate();
    ResponseEntity<PrimeNumberResponse> result = null;
    for (int i = 0; i < 10; i++) {
      result = this.restTemplate.getForEntity(uri, PrimeNumberResponse.class);
    }
    assertNotNull(result);
    assertEquals(429, result.getStatusCodeValue());
  }

  private String getBasePath() {
    return "http://localhost:" + randomServerPort;
  }

  private void setRandomIdHeadersToRestTemplate() {
    restTemplate.getRestTemplate().setInterceptors(
        Collections.singletonList((request, body, execution) -> {
          request.getHeaders()
              .add("X-Forwarded-For", UUID.randomUUID().toString());
          return execution.execute(request, body);
        }));
  }

  private void setFixedIdHeadersToRestTemplate() {
    restTemplate.getRestTemplate().setInterceptors(
        Collections.singletonList((request, body, execution) -> {
          request.getHeaders()
              .add("X-Forwarded-For", "0.0.0.0");
          return execution.execute(request, body);
        }));
  }
}