package com.nikolaynikolov.primenumberapi.cache;

public interface Cache<K, V> {
  V get(K key);

  void set(K key, V value);

  boolean contains(K key);
}
