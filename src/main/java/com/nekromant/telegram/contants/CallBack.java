package com.nekromant.telegram.contants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CallBack {
    APPROVE("/approve"),
    DENY("/deny"),
    DATE_TIME("/date_time"),
    DENY_REPORT("/cancel_report");

    private final String alias;

    CallBack(String alias) {
        this.alias = alias;
    }

    public static CallBack from(String alias) {
        return Arrays.stream(CallBack.values())
                .filter(callBack -> callBack.getAlias().equals(alias))
                .findFirst()
                .orElse(null);
    }
}
