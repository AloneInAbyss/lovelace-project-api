package br.com.fiap.lovelace_project_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private String secret;
    
    private Long expiration;
    
    private Long refreshExpiration;
    
}
