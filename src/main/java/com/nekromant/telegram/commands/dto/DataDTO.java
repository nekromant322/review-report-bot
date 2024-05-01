package com.nekromant.telegram.commands.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DataDTO {
    private String status;
    private String  number;
    private String created;
    private int interval;
    private String paymentUrl;
    private String paymentUrlWeb;
}
