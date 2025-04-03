package com.nekromant.telegram.dto;


import lombok.Data;

@Data
public class UtmDTO {

    private String utmSource;

    private String utmMedium;

    private String utmContent;

    private String utmCampaign;

    private String section;



    public static String[] utmKeys = {"utm_source", "utm_medium", "utm_campaign", "utm_content"};
}



