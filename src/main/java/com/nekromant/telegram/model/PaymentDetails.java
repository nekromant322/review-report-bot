package com.nekromant.telegram.model;

import com.nekromant.telegram.commands.dto.*;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.converter.OrderJsonType;
import com.nekromant.telegram.converter.PurchaseJsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDefs(
        {
                @TypeDef(name = "PurchaseJsonType", typeClass = PurchaseJsonType.class),
                @TypeDef(name = "OrderJsonType", typeClass = OrderJsonType.class)
        }
)

public class PaymentDetails {
    @Id
    private String number;

    @Column
    private String originalNumber;

    @Column(name = "td_type")
    private String type;

    @Column
    private PayStatus status;

    @Column
    private String method;

    @Column
    private String terminalSerial;

    @Column
    private String recipientInn;

    @Column
    private String operatorLogin;

    @Column
    private String operatorName;

    @Column
    private String amount;

    @Column
    private String tipAmount;

    @Column
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
    private String cardHolder;

    @Column
    private String rrn;

    @Column
    private double lat;

    @Column
    private double lng;

    @Column
    private String created;

    @Column
    private ServiceType serviceType;

    @Convert(disableConversion = true)
    @Type(type = "PurchaseJsonType")
    @Column(columnDefinition = "jsonb")
    private PurchaseDTO[] purchase;

    @Convert(disableConversion = true)
    @Type(type = "OrderJsonType")
    @Column(name = "td_order")
    private OrderDTO order;
}
