package com.nekromant.telegram.contants;

import java.util.Arrays;

public enum ServiceType {
    RESUME("resume"),
    MENTORING("mentoring");

    private String alias;

    ServiceType(String alias) {
        this.alias = alias;
    }

    public String get() {
        return alias;
    }

    public static ServiceType getServiceByAlias(String alias) {
        return Arrays.stream(ServiceType.values())
                .filter(service -> service.alias.equalsIgnoreCase(alias))
                .findFirst().get();
    }
}
