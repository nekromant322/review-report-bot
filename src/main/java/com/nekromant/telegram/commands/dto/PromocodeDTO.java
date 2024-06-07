package com.nekromant.telegram.commands.dto;

import com.nekromant.telegram.contants.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromocodeDTO {
    private Long id;

    private String promocodeText;

    private double discountPercent;

    private int maxUsesNumber;

    private boolean isActive;

    private ServiceType serviceType;
}
