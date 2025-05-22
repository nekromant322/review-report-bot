package com.nekromant.telegram.service;

import com.nekromant.telegram.model.UtmTag;
import com.nekromant.telegram.repository.UtmTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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

        return Arrays.stream(cookies)
                .filter(cookie -> "utm_source".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> getTag(cookie.getValue(), UtmTag.PAY_REQUEST))
                .orElse(null);
    }


    public UtmTag getTag(String source, String section) {
        UtmTag tag = UtmTag.builder()
                .section(section)
                .localDateTime(LocalDateTime.now())
                .source(source)
                .build();

        utmTagRepository.save(tag);

        return tag;
    }

    public Cookie setCookieByUtmTag(UtmTag utmTag) {
        if (utmTag != null) {
            Cookie sourceCookie = new Cookie("utm_source", utmTag.getSource());
            sourceCookie.setPath("/");
            sourceCookie.setMaxAge(60 * 60 * 24 * 30);
            return sourceCookie;
        }
        return null;
    }

}
