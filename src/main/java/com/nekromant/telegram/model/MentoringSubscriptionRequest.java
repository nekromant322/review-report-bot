package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MentoringSubscriptionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Exclude
    private Long id;

    private String tgName;

    private String customerPhone;

    private String lifePayTransactionNumber;
}
