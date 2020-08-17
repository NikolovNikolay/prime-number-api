package com.nikolaynikolov.primenumberapi.service;

import com.nikolaynikolov.primenumberapi.model.User;
import org.redisson.api.RMap;
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
    RMap<String, User> userMap = redissonClient.getMap(USER_MAP);
    return userMap.get(key);
  }

  public void setUser(String key, User user) {
    RMapCache<String, User> userMap = redissonClient.getMapCache(USER_MAP);
    userMap.put(key, user, 1, TimeUnit.HOURS);
  }

  public void setPrimeNumbers(List<Long> numbers) {
    RSetCache<Long> primeNumberSet = redissonClient.getSetCache(PRIME_NUMBER_SET);
    primeNumberSet.addAllAsync(numbers);
  }

  public boolean checkIfPrimeNumber(Long number) {
    RSetCache<Long> primeNumberSet = redissonClient.getSetCache(PRIME_NUMBER_SET);
    return primeNumberSet.contains(number);
  }

  public Long getNextPrimeNumber(Long number) {
    for (long i = number; i < number * 2; i++) {
      if (checkIfPrimeNumber(i)) {
        return i;
      }
    }
    return null;
  }
}
