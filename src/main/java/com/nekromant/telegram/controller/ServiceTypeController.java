package com.nekromant.telegram.controller;

import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.service.PromocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.Set;

@RestController
@RequestMapping("/servicetypes")
public class ServiceTypeController {
    @Autowired
    private PromocodeService promocodeService;

    @GetMapping
    public Set getServiceTypes() {
        return EnumSet.allOf(ServiceType.class);
    }

    @GetMapping("/checkpromocode")
    public ResponseEntity checkPromocode(@RequestParam("service_type") ServiceType serviceType,
                                         @RequestParam("promocode_id") String promocode_id) {
        return ResponseEntity.ok(promocodeService.findById(promocode_id).getServiceType() == serviceType);
    }
}
