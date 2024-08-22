package com.nekromant.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "price")
@Getter
@Setter
public class PriceProperties {

    private String resumeReview;

    private String mentoringSubscription;

    private String personalCall;

    private String userName;
}
