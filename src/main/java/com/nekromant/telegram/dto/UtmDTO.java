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

    @Override
    public String toString() {
        return String.join("|",
                utmSource,
                utmMedium,
                utmContent,
                utmCampaign,
                section
        );
    }


    public static UtmDTO fromString(String dtoString) {
        String[] parts = dtoString.split("\\|");
        UtmDTO dto = new UtmDTO();
        dto.setUtmSource(getPartOrDefault(parts, 0));
        dto.setUtmMedium(getPartOrDefault(parts, 1));
        dto.setUtmContent(getPartOrDefault(parts, 2));
        dto.setUtmCampaign(getPartOrDefault(parts, 3));
        dto.setSection(getPartOrDefault(parts, 4));
        return dto;
    }

    private static String getPartOrDefault(String[] parts, int index) {
        return (parts.length > index && parts[index] != null && !parts[index].isBlank()) ? parts[index] : "notSet";
    }

}



