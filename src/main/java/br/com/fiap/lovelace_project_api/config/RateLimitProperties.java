package br.com.fiap.lovelace_project_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {
    // Limits are defined as capacity per time window (milliseconds)
    // Default values (tunable)
    private int loginCapacity = 5;
    private long loginWindowMs = 60000; // 1 minute

    private int refreshCapacity = 30;
    private long refreshWindowMs = 60000; // 1 minute

    private int globalCapacity = 100;
    private long globalWindowMs = 60000; // 1 minute
}
