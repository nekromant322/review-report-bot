package com.nekromant.telegram.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@Slf4j
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UtmTags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utm_source")
    private String utmSource;

    @Column(name = "utm_medium")
    private String utmMedium;

    @Column(name = "utm_content")
    private String utmContent;

    @Column(name = "utm_campaign")
    private String utmCampaign;

    @Column(name = "value_clicks")
    private Integer valueClicks;

    @Column(name="section_site")
    private String section;

    @Column(name  = "payments")
    @OneToMany(mappedBy = "utmTags")
    private List<PaymentDetails> payments;
}
