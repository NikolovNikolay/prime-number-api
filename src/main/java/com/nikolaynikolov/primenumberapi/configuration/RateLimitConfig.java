package com.nikolaynikolov.primenumberapi.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rl")
public class RateLimitConfig {

  private int maxPerMinute;
  private int maxPerSecond;
}
