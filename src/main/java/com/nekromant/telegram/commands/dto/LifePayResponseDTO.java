package com.nekromant.telegram.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LifePayResponseDTO {
    private String code;
    private String message;
    private LifePayResponseDataDTO data;
}
