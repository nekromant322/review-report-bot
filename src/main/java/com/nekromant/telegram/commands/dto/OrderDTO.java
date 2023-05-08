package com.nekromant.telegram.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO implements Serializable {
    private String ext_id;
    private String number;
    private String name;
    private String phone;
    private String email;
    private String comment;
    private String barcode;
}
