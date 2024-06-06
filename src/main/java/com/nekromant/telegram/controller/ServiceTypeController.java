package com.nekromant.telegram.controller;

import com.nekromant.telegram.contants.ServiceType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.Set;

@RestController
@RequestMapping("/servicetypes")
public class ServiceTypeController {
    @GetMapping
    public Set getServiceTypes() {
        return EnumSet.allOf(ServiceType.class);
    }
}
