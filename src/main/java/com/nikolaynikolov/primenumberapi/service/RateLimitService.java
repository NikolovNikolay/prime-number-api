package com.nikolaynikolov.primenumberapi.service;

import com.nikolaynikolov.primenumberapi.configuration.RateLimitConfig;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private final RedissonClient redissonClient;
  private final RateLimitConfig rateLimiterConfig;

  @Autowired
  public RateLimitService(RedissonClient redissonClient,
                          RateLimitConfig rateLimiterConfig) {
    this.redissonClient = redissonClient;
    this.rateLimiterConfig = rateLimiterConfig;
  }

  public boolean canProceed(String ip) {
    var rateLimiter = redissonClient.getRateLimiter(ip);
    rateLimiter.trySetRate(RateType.PER_CLIENT, rateLimiterConfig.getMaxPerMinute(), 1, RateIntervalUnit.MINUTES);
    rateLimiter.trySetRate(RateType.PER_CLIENT, rateLimiterConfig.getMaxPerSecond(), 1, RateIntervalUnit.SECONDS);
    return rateLimiter.tryAcquire();
  }
}
