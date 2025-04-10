package com.nekromant.telegram.config;

import com.nekromant.telegram.dto.UtmDTO;
import com.nekromant.telegram.model.UtmTags;
import com.nekromant.telegram.repository.UtmTagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UtmInterceptor implements HandlerInterceptor {

    private final UtmTagsRepository utmRepository;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        log.info("Перехватили запрос");
        boolean[] cookieTag = {false};
        UtmDTO utmDto = new UtmDTO();
        String controllerName;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            controllerName = handlerMethod.getBeanType().getSimpleName();
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
                log.warn("cookiesValue: " + cookiesValue);
                cookieTag[0] = true;
                setUtmField(utmDto, keys, cookiesValue);
            }
        });

        if (cookieTag[0]) {
            log.info("UTM-метки получены из файлов cookies");
        } else {
            UtmDTO validatedDto = validateDTO(utmDto);
            if (validatedDto != null) {
                saveUtmTag(validatedDto);
                log.info("UTM-метки распаршены и сохранены UtmDTO: {}", validatedDto);
            } else {
                log.info("UTM-метки не найдены");
                request.setAttribute("utmDto", "notSet");
                log.info("Передаем запрос контроллеру после парсинга меток");
                return true;
            }
        }

        request.setAttribute("utmDto", utmDto.toString());
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


    private UtmDTO validateDTO(UtmDTO utmDTO) {
        boolean allNullOrEmpty = isNullOrEmpty(utmDTO.getUtmSource()) &&
                isNullOrEmpty(utmDTO.getUtmMedium()) &&
                isNullOrEmpty(utmDTO.getUtmContent()) &&
                isNullOrEmpty(utmDTO.getUtmCampaign());

        if (allNullOrEmpty) {
            return null;
        }

        if (isNullOrEmpty(utmDTO.getUtmSource())) utmDTO.setUtmSource("notSet");
        if (isNullOrEmpty(utmDTO.getUtmMedium())) utmDTO.setUtmMedium("notSet");
        if (isNullOrEmpty(utmDTO.getUtmContent())) utmDTO.setUtmContent("notSet");
        if (isNullOrEmpty(utmDTO.getUtmCampaign())) utmDTO.setUtmCampaign("notSet");
        if (isNullOrEmpty(utmDTO.getSection())) utmDTO.setSection("notSet");
        return utmDTO;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    private void saveUtmTag(UtmDTO utmDTO) {
        UtmTags tags;
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
                    .payments(new ArrayList<>())
                    .build();
            utmRepository.save(tags);
        }

    }
}
