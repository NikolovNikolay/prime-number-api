package com.nikolaynikolov.primenumberapi.configuration;

import jodd.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "scheduledThreadPoolExecutor")
  public ScheduledExecutorService executorService() {
    return new ScheduledThreadPoolExecutor(
        2,
        ThreadFactoryBuilder.create()
            .setNameFormat("ScheduledThread").get());
  }
}
