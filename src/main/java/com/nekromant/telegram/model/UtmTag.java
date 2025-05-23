package com.nekromant.telegram.model;

import lombok.*;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtmTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;

    private LocalDateTime localDateTime;

    private String section;

    public static String PRICE_SECTION = "price_section";

    public static String PAY_REQUEST = "pay_request";
}
