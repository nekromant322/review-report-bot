package com.nekromant.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nekromant.telegram.converter.AddFieldsConverter;
import com.nekromant.telegram.converter.OrderConverter;
import com.nekromant.telegram.converter.OriginalAddFieldsConverter;
import com.nekromant.telegram.converter.PurchaseConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetails {
    @Id
    private String number;

    @Column
    @JsonProperty(value = "original_number")
    private String originalNumber;

    @Column
    private String type;

    @Column
    private String status;

    @Column
    private String method;

    @Column
    @JsonProperty(value = "terminal_serial")
    private String terminalSerial;

    @Column
    @JsonProperty(value = "recipient_inn")
    private String recipientInn;

    @Column
    @JsonProperty(value = "operator_login")
    private String operatorLogin;

    @Column
    @JsonProperty(value = "operator_name")
    private String operatorName;

    @Column
    private String amount;

    @Column
    @JsonProperty(value = "tip_amount")
    private String tipAmount;

    @Column
    @JsonProperty(value = "discount_amount")
    private String discountAmount;

    @Column
    private String description;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private String pan;

    @Column
    private String cardholder;

    @Column
    private String rrn;

    @Column
    private double lat;

    @Column
    private double lng;

    @Column
    private String created;

    @Convert(converter = PurchaseConverter.class)
    @Column(columnDefinition = "jsonb")
    private Purchase[] purchase;

    @Convert(converter = OrderConverter.class)
    @Column(columnDefinition = "jsonb")
    private Order order;

    @Convert(converter = AddFieldsConverter.class)
    @Column(columnDefinition = "jsonb")
    @JsonProperty(value = "add_fields")
    private AddFields addFields;

    @Convert(converter = OriginalAddFieldsConverter.class)
    @Column(columnDefinition = "jsonb")
    @JsonProperty(value = "original_add_fields")
    private OriginalAddFields originalAddFields;
}
