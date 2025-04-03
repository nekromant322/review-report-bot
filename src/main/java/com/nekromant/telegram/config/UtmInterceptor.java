package com.nekromant.telegram.config;

import com.nekromant.telegram.dto.UtmDTO;
import com.nekromant.telegram.model.UtmTags;
import com.nekromant.telegram.repository.UtmTagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor

public class UtmInterceptor implements HandlerInterceptor {

    private final UtmTagsRepository utmRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UtmDTO utmDto = new UtmDTO();

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            log.info("Запрос обработан в контроллере: " + controllerName + ", метод: " + methodName);
            utmDto.setSection(controllerName + "-" + methodName);
        }

        Arrays.stream(UtmDTO.utmKeys).forEach(keys -> {

            String urlValue = request.getParameter(keys);
            String cookiesValue = getCookiesValue(request, keys);
            if (urlValue != null) {
                setCookie(response, keys, urlValue);
                setUtmField(utmDto, keys, urlValue);
            } else if (cookiesValue != null) {
                setUtmField(utmDto, keys, cookiesValue);
            }
        });

        if (validateDTO(utmDto)) {
            saveUtmTag(utmDto);
            log.info("UTM-метки распаршены и сохранены UtmDTO: " + utmDto);
        } else {
            log.info("UTM-метки не найдены");
        }


        log.info("Передаем запрос контроллеру после парсинга меток");
        return true;
    }


    private String getCookiesValue(HttpServletRequest request, String param) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> param.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
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


    private boolean validateDTO(UtmDTO utmDTO) {
        if (true) {
            saveUtmTag(utmDTO);
        }
        return true;
    }


    private void saveUtmTag(UtmDTO utmDTO) {
        UtmTags tags = null;
        Optional<UtmTags> utmTags = utmRepository.findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaign(utmDTO.getUtmSource(),
                utmDTO.getUtmMedium(),
                utmDTO.getUtmContent(),
                utmDTO.getUtmCampaign());

        if (utmTags.isPresent()) {
            tags = utmTags.get();
            tags.setValueClicks(utmTags.get().getValueClicks() + 1);
            utmRepository.save(tags);
        } else {
            tags = UtmTags.builder()
                    .utmSource(utmDTO.getUtmSource())
                    .utmMedium(utmDTO.getUtmMedium())
                    .utmContent(utmDTO.getUtmContent())
                    .utmCampaign(utmDTO.getUtmCampaign())
                    .section(utmDTO.getSection())
                    .valueClicks(1)
                    .build();
            utmRepository.save(tags);
        }

    }
}
