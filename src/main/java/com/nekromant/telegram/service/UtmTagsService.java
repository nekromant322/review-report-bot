package com.nekromant.telegram.service;

import com.nekromant.telegram.dto.UtmDTO;
import com.nekromant.telegram.model.UtmTags;
import com.nekromant.telegram.repository.UtmTagsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtmTagsService {

    private final UtmTagsRepository utmRepository;

    //TODO Логирование + подумать как учитывать в оплатах переход с меткой и обратно куку привязывать нннада

    public String parseUtmTags(HttpServletRequest request, String section) {
        UtmDTO utmDTO = new UtmDTO();
        utmDTO.setSection(section);

        Arrays.stream(UtmDTO.utmKeys).forEach(keys -> {

            String urlValue = request.getParameter(keys);
            String cookiesValue = getCookiesValue(request, keys);




                }


        );


        return null;
    }

    private String getCookiesValue(HttpServletRequest request, String param) {


        return null;
    }



    private void setUtmField(UtmDTO dto, String key, String value) {
        switch (key) {
            case "utm_source":
                dto.setUtmSource(value);
                break;
            case "utm_medium":
                dto.setUtmMedium(value);
                break;
            case "utm_campaign":
                dto.setUtmCampaign(value);
                break;
            case "utm_content":
                dto.setUtmContent(value);
                break;
        }
    }
    //TODO Сделать валидацию на полное дто
    private void validateDTO(UtmDTO utmDTO){
        if(true){
            saveUtmTag(utmDTO);
        }
    }



    private void saveUtmTag(UtmDTO utmDTO){
        UtmTags tags = null;
        Optional<UtmTags> utmTags = utmRepository.findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaign(utmDTO.getUtmSource(),
                utmDTO.getUtmMedium(),
                utmDTO.getUtmContent(),
                utmDTO.getUtmCampaign());

        if(utmTags.isPresent()){
            tags = utmTags.get();
            tags.setValueClicks(utmTags.get().getValueClicks() + 1);
            utmRepository.save(tags);
        } else {
            tags = UtmTags.builder()
                    .utmSource(utmDTO.getUtmSource())
                    .utmMedium(utmDTO.getUtmMedium())
                    .utmContent(utmDTO.getUtmContent())
                    .utmCampaign(utmDTO.getUtmCampaign())
                    .section(UtmTags.parseSection(utmDTO.getSection()))
                    .valueClicks(1)
                    .build();
            utmRepository.save(tags);
        }

    }
}
