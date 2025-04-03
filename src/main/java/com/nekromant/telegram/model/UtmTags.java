package com.nekromant.telegram.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "section")
    private Section section;


    public static enum Section {
        MAIN_PAGE,
        PRICE_PAGE;
    }

    public static Section parseSection(String section){
        switch (section){
            case "main":
                return Section.MAIN_PAGE;
            case "price":
                return Section.PRICE_PAGE;
            default:
                log.error("Значение для определения страницы получения меток не расознано, значение: " + section);
                throw new RuntimeException("Значение не распознанно");
        }
    }
}
