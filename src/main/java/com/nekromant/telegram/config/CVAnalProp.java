package com.nekromant.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class CVAnalProp {
    @Value("${pay-info.login}")
    private String login;

    @Value("${pay-info.apikey}")
    private String apikey;

    @Value("${pay-info.method}")
    private String method;

    @Value("${price.resume-review}")
    private String amount;

    @Value("${owner.userName}")
    private String receiverName;
}
