package com.nikolaynikolov.primenumberapi;

import com.nikolaynikolov.primenumberapi.configuration.PrimeCalculationConfig;
import com.nikolaynikolov.primenumberapi.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SieveOfEratosthenesRunner implements ApplicationRunner {

  private final ScheduledExecutorService executor;
  private final CacheService cacheService;
  private final PrimeCalculationConfig primeCalculationConfig;

  @Autowired
  public SieveOfEratosthenesRunner(@Qualifier("scheduledThreadPoolExecutor") ScheduledExecutorService executor,
                                   CacheService cacheService,
                                   PrimeCalculationConfig primeCalculationConfig) {
    this.executor = executor;
    this.cacheService = cacheService;
    this.primeCalculationConfig = primeCalculationConfig;
  }

  @Override
  public void run(ApplicationArguments args) {
    long maxPrime = primeCalculationConfig.getMax();
    executor.schedule(new PrimeNumberSieveRunnable(cacheService, 2, maxPrime / 2), 0, TimeUnit.SECONDS);
    executor.schedule(new PrimeNumberSieveRunnable(cacheService, (maxPrime / 2 + 1), maxPrime), 0, TimeUnit.SECONDS);
  }

  @Slf4j
  private static class PrimeNumberSieveRunnable implements Runnable {

    private final CacheService cacheService;
    private final long start;
    private final long end;

    PrimeNumberSieveRunnable(CacheService cacheService, long start, long end) {
      this.cacheService = cacheService;
      this.start = start;
      this.end = end;
    }

    @Override
    public void run() {
      var primes = new ArrayList<Long>();

      log.info("Starting prime numbers pre-calculation");
      for (long i = start; i < end; i++) {
        boolean isPrime = isPrime(2, i);
        if (isPrime) {
          primes.add(i);
        }

        if (primes.size() == 100) {
          cacheService.setPrimeNumbers(primes);
          primes.clear();
        }
      }
      log.info("Prime numbers pre-calculation finished");
    }

    private boolean isPrime(long start, long check) {
      while (start * start <= check) {
        if (check % start == 0) {
          return false;
        }
        start++;
      }
      return true;
    }
  }
}
