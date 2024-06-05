package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Promocode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String promocodeText;

    private double discountPercent;

    private LocalDateTime created;

    private int counterUsed;

    private int maxUsesNumber;

    private boolean isActive;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<PaymentDetails> paymentDetailsSet;
}
