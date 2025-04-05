package com.nekromant.telegram.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@Slf4j
@Entity
@RequiredArgsConstructor
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

    @OneToMany(mappedBy = "utmTags")
    private List<PaymentDetails> payments;

}
