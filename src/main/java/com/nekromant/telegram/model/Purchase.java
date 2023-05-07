package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Purchase{
    private String name;
    private String quantity;
    private String unit;
    private String amount;
    private String ext_id;
}
