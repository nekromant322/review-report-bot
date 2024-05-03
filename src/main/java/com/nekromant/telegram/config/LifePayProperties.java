package com.nekromant.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pay-info")
@Getter
@Setter
public class LifePayProperties {
    private String login;

    private String apikey;

    private String method;
}
