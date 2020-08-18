package com.nikolaynikolov.primenumberapi;

import com.nikolaynikolov.primenumberapi.configuration.RedisConfig;
import org.junit.ClassRule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PrimeapiContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @ClassRule
  public static GenericContainer redis = new GenericContainer("redis:latest")
      .withExtraHost("localhost", "0.0.0.0")
      .withExposedPorts(6379);

  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres")
      .withDatabaseName("postgres")
      .withPassword("admin")
      .withUsername("admin");

  @ClassRule
  public static GenericContainer prometheus = new GenericContainer("prom/prometheus")
      .withExtraHost("localhost", "0.0.0.0")
      .withExposedPorts(9090);

  @TestConfiguration
  static class LiquibaseUserConfiguration {
    @Bean
    RedisConfig theRedisConfig() {
      var redisConfig = new RedisConfig();
      redisConfig.setHost(redis.getContainerIpAddress());
      redisConfig.setPort(redis.getMappedPort(6379));
      return redisConfig;
    }
  }

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    postgres.start();
    redis.start();
    prometheus.start();

    TestPropertyValues.of(
        "prime.max=10",
        "rl.maxPerSecond=5",
        "spring.liquibase.enabled=false",
        "spring.datasource.url=" + postgres.getJdbcUrl(),
        "spring.datasource.username=admin",
        "spring.datasource.password=admin",
        "redis.host=" + redis.getContainerIpAddress(),
        "redis.port=" + redis.getMappedPort(6379)
    ).applyTo(configurableApplicationContext.getEnvironment());
  }
}
