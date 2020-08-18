package com.nikolaynikolov.primenumberapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "threadPoolTaskExecutor")
  public ThreadPoolTaskExecutor executorService() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setMaxPoolSize(5);
    executor.setCorePoolSize(3);
    executor.setThreadNamePrefix("PrimeApiThreadPool");
    executor.initialize();

    return executor;
  }
}
