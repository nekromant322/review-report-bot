package com.nekromant.telegram.service;

import com.nekromant.telegram.model.UtmTag;
import com.nekromant.telegram.repository.UtmTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UtmTagsService {

    private final UtmTagRepository utmTagRepository;

    public UtmTag getUtmSourceFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        UtmTag tag = Arrays.stream(cookies)
                .filter(cookie -> "utm_source".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> getTag(cookie.getValue(), UtmTag.PAY_REQUEST))
                .orElse(null);
        return tag;
    }


    public UtmTag getTag(String tag, String section) {
        UtmTag tagEntity = utmTagRepository.getUtmTagsBySourceAndSection(tag, section)
                .map(existingTag -> {
                    existingTag.setValueClick(existingTag.getValueClick() + 1);
                    return existingTag;
                })
                .orElseGet(() -> UtmTag.builder()
                        .source(tag)
                        .valueClick(1)
                        .section(section)
                        .build());

        utmTagRepository.save(tagEntity);

        return tagEntity;
    }

    public Cookie setCookieByUtmTag(UtmTag utmTag){
        if(utmTag != null){
            Cookie sourceCookie = new Cookie("utm_source", utmTag.getSource());
            sourceCookie.setPath("/");
            sourceCookie.setMaxAge(60 * 60 * 24 * 30);
            return  sourceCookie;
        }
        return null;
    }

}
