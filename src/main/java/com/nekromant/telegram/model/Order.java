package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order{
    private String ext_id;
    private String number;
    private String name;
    private String phone;
    private String email;
    private String comment;
    private String barcode;
}
