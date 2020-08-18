package com.nikolaynikolov.primenumberapi;

import com.nikolaynikolov.primenumberapi.model.Permission;
import com.nikolaynikolov.primenumberapi.model.User;
import com.nikolaynikolov.primenumberapi.service.CacheService;
import com.nikolaynikolov.primenumberapi.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

import static com.nikolaynikolov.primenumberapi.model.Permission.ACCESS_PRIME_API;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {PrimeapiContextInitializer.class})
class PrimeapiApplicationTests {

  @Autowired
  private RateLimitService rateLimitService;

  @Autowired
  private CacheService cacheService;

  @Test
  public void contextLoads() {
  }

  @Test
  public void testRateLimiting_shouldNotExceed() {

    String ip = "123.34.0.1";
    assertTrue(rateLimitService.canProceed(ip));
  }

  @Test
  public void testRateLimiting_shouldExceed() {

    String ip = "123.34.0.2";

    for (int i = 0; i < 100; i++) {
      rateLimitService.canProceed(ip);
    }
    assertFalse(rateLimitService.canProceed(ip));
  }

  @Test
  public void testCacheService_shouldSetPrimeNumberCorrectly_andReturnThemBack() {

    var listOfPrimes = List.of(1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867);
    cacheService.setPrimeNumbers(listOfPrimes);
    assertTrue(cacheService.checkIfPrimeNumber(1801));
    assertTrue(cacheService.checkIfPrimeNumber(1741));
    assertTrue(cacheService.checkIfPrimeNumber(1789));

    assertFalse(cacheService.checkIfPrimeNumber(346));
    assertFalse(cacheService.checkIfPrimeNumber(1760));
  }

  @Test
  public void testCacheService_getNextPrimeNumber_shouldGetNextPrimeNumberCorrectly() {

    var listOfPrimes = List.of(1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867);
    cacheService.setPrimeNumbers(listOfPrimes);

    var nextPrime = cacheService.getNextPrimeNumber(1741);
    assertEquals(1747, nextPrime.intValue());
    nextPrime = cacheService.getNextPrimeNumber(1847);
    assertEquals(1861, nextPrime.intValue());
  }

  @Test
  public void testCacheService_getNextPrimeNumber_shouldReturnNullIfPassedNull() {

    var listOfPrimes = List.of(1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867);
    cacheService.setPrimeNumbers(listOfPrimes);
    var nextPrime = cacheService.getNextPrimeNumber(null);
    assertNull(nextPrime);
  }

  @Test
  public void testCacheService_getNextPrimeNumber_shouldReturnNullIfNextPrimeIsNotAvailable() {

    var listOfPrimes = List.of(1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867);
    cacheService.disposeSetPrimeNumberCache();
    cacheService.setPrimeNumbers(listOfPrimes);
    var nextPrime = cacheService.getNextPrimeNumber(1867);
    assertNull(nextPrime);
  }

  @Test
  public void testCacheService_setUser_shouldSetCorrectly() {
    User u = new User();
    u.setId(123L);
    u.setKey("hello");
    u.setName("John");
    u.setPermissions(Set.of(new Permission(ACCESS_PRIME_API)));

    cacheService.setUser(u.getKey(), u);

    var cacheUser = cacheService.getUser(u.getKey());
    assertNotNull(cacheUser);
    assertEquals(123, cacheUser.getId().longValue());
  }
}
