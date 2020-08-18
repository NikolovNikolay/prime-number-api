package com.nikolaynikolov.primenumberapi.service;

import com.nikolaynikolov.primenumberapi.model.User;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

  private static final String USER_MAP = "userMap";
  private static final String PRIME_NUMBER_SET = "primeNumberSet";

  private final RedissonClient redissonClient;

  @Autowired
  public CacheService(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  public User getUser(String key) {
    RMapCache<String, User> userMap = redissonClient.getMapCache(USER_MAP);
    return userMap.get(key);
  }

  public void setUser(String key, User user) {
    RMapCache<String, User> userMap = redissonClient.getMapCache(USER_MAP);
    userMap.put(key, user, 1, TimeUnit.HOURS);
  }

  public void setPrimeNumbers(List<Integer> numbers) {
    if (!redissonClient.isShutdown()) {
      RSetCache<Integer> primeNumberSet = redissonClient.getSetCache(PRIME_NUMBER_SET);
      primeNumberSet.addAll(numbers);
    }
  }

  public boolean checkIfPrimeNumber(Integer number) {
    if (!redissonClient.isShutdown()) {
      RSetCache<Integer> primeNumberSet = redissonClient.getSetCache(PRIME_NUMBER_SET);
      return primeNumberSet.contains(number);
    }
    throw new RedissonShutdownException("Can't check number - redisson is shutdown");
  }

  public Integer getNextPrimeNumber(Integer number) {
    if (number == null) {
      return null;
    }
    for (int i = number + 1; i < number * 2; i++) {
      if (checkIfPrimeNumber(i)) {
        return i;
      }
    }
    return null;
  }

  public void disposeSetPrimeNumberCache() {
    if (!redissonClient.isShutdown()) {
      RSetCache<Integer> primeNumberSet = redissonClient.getSetCache(PRIME_NUMBER_SET);
      primeNumberSet.clear();
    }
  }
}
