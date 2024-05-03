package com.nekromant.telegram.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LifePayResponseDataDTO {
    private String status;
    private String number;
    private String created;
    private int interval;
    private String paymentUrl;
    private String paymentUrlWeb;
}
