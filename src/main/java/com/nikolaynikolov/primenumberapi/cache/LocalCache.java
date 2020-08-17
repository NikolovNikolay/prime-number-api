package com.nikolaynikolov.primenumberapi.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class LocalCache<K, V> implements com.nikolaynikolov.primenumberapi.cache.Cache<K, V> {

  private final Cache<K, V> cache;

  public LocalCache() {
    cache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();
  }

  @Override
  public V get(K key) {
    return this.cache.getIfPresent(key);
  }

  @Override
  public void set(K key, V value) {
    this.cache.put(key, value);
  }

  @Override
  public boolean contains(K key) {
    return this.get(key) != null;
  }
}
