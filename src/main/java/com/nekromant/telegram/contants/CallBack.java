package com.nekromant.telegram.contants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CallBack {
    APPROVE("approve"),
    DENY("deny"),
    DATE_TIME("dateTime"),
    DENY_REPORT("denyReport");

    private final String alias;

    CallBack(String alias) {
        this.alias = alias;
    }

    public static CallBack from(String alias) {
        return Arrays.stream(CallBack.values())
                .filter(callBack -> callBack.getAlias().equalsIgnoreCase(alias))
                .findFirst()
                .orElse(null);
    }
}
