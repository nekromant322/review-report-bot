package com.nekromant.telegram.commands.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChequeDTO {
    @JsonProperty(value = "login")
    private String login;

    @JsonProperty(value = "apikey")
    private String apiKey;

    @JsonProperty(value = "amount")
    private String amount;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "customer_phone")
    private String customerPhone;

    @JsonProperty(value = "method")
    private String method;
}
