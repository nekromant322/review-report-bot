package com.nekromant.telegram.commands.feign;

import com.nekromant.telegram.model.Cheque;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LifePayApi", url = "https://api.life-pay.ru/")
public interface LifePayFeign {
    @PostMapping(value = "v1/bill")
    ResponseEntity<String> payCheque(@RequestBody Cheque cheque);
}
