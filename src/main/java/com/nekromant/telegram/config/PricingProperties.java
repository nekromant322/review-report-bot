package com.nekromant.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class PricingProperties {

    private String login;

    private String apikey;

    private String method;

    private String resumeReview;

    private String userName;

    @Bean
    @ConfigurationProperties(prefix = "pay-info")
    public PricingProperties PayInfoProperties() {
        return new PricingProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "price")
    public PricingProperties PriceProperties() {
        return new PricingProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "owner")
    public PricingProperties OwnerProperties() {
        return new PricingProperties();
    }
}
