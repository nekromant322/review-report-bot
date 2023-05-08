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
public class PaymentDetailsDTO {
    private String number;
    @JsonProperty(value = "original_number")
    private String originalNumber;
    private String type;
    private String status;
    private String method;
    @JsonProperty(value = "terminal_serial")
    private String terminalSerial;
    @JsonProperty(value = "recipient_inn")
    private String recipientInn;
    @JsonProperty(value = "operator_login")
    private String operatorLogin;
    @JsonProperty(value = "operator_name")
    private String operatorName;
    private String amount;
    @JsonProperty(value = "tip_amount")
    private String tipAmount;
    @JsonProperty(value = "discount_amount")
    private String discountAmount;
    private String description;
    private String phone;
    private String email;
    private String pan;
    @JsonProperty(value = "cardholder")
    private String cardHolder;
    private String rrn;
    private double lat;
    private double lng;
    private String created;
    @JsonProperty(value = "purchase")
    private PurchaseDTO[] purchaseDTO;
    @JsonProperty(value = "order")
    private OrderDTO orderDTO;
    @JsonProperty(value = "add_fields")
    private AddFieldsDTO addFieldsDTO;
    @JsonProperty(value = "original_add_fields")
    private OriginalAddFieldsDTO originalAddFieldsDTO;
}
