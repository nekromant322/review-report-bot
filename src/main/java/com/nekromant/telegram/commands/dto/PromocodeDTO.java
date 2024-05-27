package com.nekromant.telegram.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromocodeDTO {

    private String promocodeText;

    private double discountPercent;

    private int maxUsesNumber;

    private boolean isActive;
}
