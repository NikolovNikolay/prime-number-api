package com.nikolaynikolov.primenumberapi;

import com.nikolaynikolov.primenumberapi.configuration.PrimeCalculationConfig;
import com.nikolaynikolov.primenumberapi.service.CacheService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link PrimeNumberPopulateRunner#run(ApplicationArguments)} method is executed on application start.
 * It will then start calculating all prime numbers in the range between 2 and 10_000_000.
 * <p>
 * The logic will split the number to several parts and utilize the configured {@link ThreadPoolTaskExecutor} in
 * {@link com.nikolaynikolov.primenumberapi.configuration.AsyncConfig} to calculate the prime numbers in parallel.
 * That way it's more likely for the API to respond correctly if the user asks to check a larger number.
 * <p>
 * Complexity:
 * O(N/2(√N) + N/4(√N) + N/4(√N)) = O((√N) * (N/2 + 2(N/4)) = O(N(√N)) = O(√N)
 * The work is split between the 3 threads, but still we are calling the isPrime(int) method which will loop from 2 to √N
 * for every single passed integer - this is also decisive for the complexity of the algorithm.
 * We are processing only odd integers as an optimisation, but still this does not reduce the worst level of complexity
 * which remains the same (O(N/4(√N) + N/8(√N) + N/8(√N)) is still O(√N))
 * <p>
 * There is the famous solution known as the Sieve of Eratosthenes for calculating all prime numbers from 2 to N (also provided
 * below). As an algorithm it's almost two times faster (O(Nlog(logN)) than the multi-thread solution. There are even solutions
 * of the Sieve of Eratosthenes with linear complexity O(N).
 * <p>
 * Memory:
 * There are 3 auxiliary array lists (one for each thread) in the code which won't exceed 100 elements in any given moment.
 * After reaching 100 elements the array lists are flushed to cache then disposed.
 * This would mean that there are ~ 2,5 kb of memory needed to store the calculated prime numbers.
 * 3 x (100 elements * 4 bytes) = 3 x 400 bytes = 1200 bytes = 1,2 kb
 * <p>
 * If we choose the Sieve of Eratosthenes solution then the memory calculations will be as follows:
 * boolean[] - 10_000_000 x 1 bit ~ 1250 kb
 * auxiliary arrayList - 100 x 4 bytes = 400 bytes, or ~ 1,3 Mb in total
 * <p>
 * All of the calculated prime numbers are kept in cache. All the needed memory to keep the prime numbers from 2 to 10_000_000
 * are:
 * ~ 665k primes x 4 bytes(int) = 2_660_000 bytes ~ 2,66 Mb
 * <p>
 */
@Component
public class PrimeNumberPopulateRunner implements ApplicationRunner {

  private final ThreadPoolTaskExecutor executor;
  private final CacheService cacheService;
  private final PrimeCalculationConfig primeCalculationConfig;

  @Autowired
  public PrimeNumberPopulateRunner(@Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor executor,
                                   CacheService cacheService,
                                   PrimeCalculationConfig primeCalculationConfig) {
    this.executor = executor;
    this.cacheService = cacheService;
    this.primeCalculationConfig = primeCalculationConfig;
  }

  @Override
  public void run(ApplicationArguments args) {
    int maxPrime = primeCalculationConfig.getMax();

    executor.execute(new SieveOfEratosthenesRunnable(cacheService, 2, maxPrime));

    executor.execute(new PrimeNumberCalculatorRunnable(cacheService, 2, maxPrime / 2));
    executor.execute(new PrimeNumberCalculatorRunnable(cacheService, (maxPrime / 2 + 1), (maxPrime / 2 + 1) + (maxPrime / 4)));
    executor.execute(new PrimeNumberCalculatorRunnable(cacheService, (maxPrime / 2 + 1) + (maxPrime / 4) + 1, maxPrime));
  }

  @Slf4j
  private static class PrimeNumberCalculatorRunnable implements Runnable {

    protected final CacheService cacheService;
    protected final int start;
    protected final int end;

    protected PrimeNumberCalculatorRunnable(CacheService cacheService, int start, int end) {
      this.cacheService = cacheService;
      this.start = start;
      this.end = end;
    }

    @Timed(longTask = true, value = "prime_number_calculation")
    @Override
    public void run() {

      // Maintaining the primes array list is considered thread safe as each of the 3 threads
      // instantiates it's own instance in heap
      var primes = new ArrayList<Integer>();
      var range = start + " to " + end;

      log.info("Starting prime numbers pre-calculation: " + range);
      for (int i = start; i < end; i++) {
        if (i != 2 && i % 2 == 0) {
          continue;
        }

        isPrime(2, i, primes);
        if (primes.size() == 100) {
          cacheService.setPrimeNumbers(primes);
          primes.clear();
        }
      }
      cacheService.setPrimeNumbers(primes);

      log.info("Prime numbers pre-calculation finished: " + range);
    }

    private void isPrime(int start, int check, List<Integer> primes) {
      while (start * start <= check) {
        if (check % start == 0) {
          return;
        }
        start++;
      }
      primes.add(check);
    }
  }

  @Slf4j
  private static class SieveOfEratosthenesRunnable extends PrimeNumberCalculatorRunnable {

    protected SieveOfEratosthenesRunnable(CacheService cacheService, int start, int end) {
      super(cacheService, start, end);
    }

    @Timed(longTask = true, value = "sieve_of_eratosthenes_calculation")
    @Override
    public void run() {
      var range = start + " to " + end;

      log.info("Starting prime numbers pre-calculation: " + range);
      boolean[] prime = new boolean[end + 1];
      for (int i = 0; i < end; i++) {
        prime[i] = true;
      }

      for (int p = 2; p * p <= end; p++) {
        if (prime[p]) {
          for (int i = p * p; i <= end; i += p)
            prime[i] = false;
        }
      }

      var primes = new ArrayList<Integer>();
      for (int i = 2; i <= end; i++) {
        if (prime[i]) {
          primes.add(i);
        }

        if (primes.size() == 100) {
          cacheService.setPrimeNumbers(primes);
          primes.clear();
        }
      }
      cacheService.setPrimeNumbers(primes);
      log.info("Prime numbers pre-calculation finished: " + range);
    }
  }
}
