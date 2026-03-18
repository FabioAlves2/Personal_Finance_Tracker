package com.example.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("security.jwt")
@Getter @Setter
public class JwtProperties {
    
    private String secretKey;
    
}
