package com.nekromant.telegram.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO implements Serializable {
    private String name;
    private String quantity;
    private String unit;
    private String amount;
    private String ext_id;
}
