package com.nekromant.telegram.commands.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LifePayResponseDTO {
    private String code;
    private String message;
    private DataDTO data;
}
