package com.nekromant.telegram.commands.feign;

import feign.form.FormData;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "TgFeign", url = "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}")
public interface TelegramFeign {
    @PostMapping(value = "/sendDocument", consumes = "multipart/form-data")
    ResponseEntity<JSONObject> sendDocument(@RequestPart("document") FormData file, @RequestParam("chat_id") String chatId);
}
