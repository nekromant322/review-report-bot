package com.nekromant.telegram.contants;

import lombok.Getter;

@Getter
public enum CallBack {
    APPROVE("/approve"),
    DENY("/deny"),
    TODAY("/today"),
    YESTERDAY("/yesterday"),
    DENY_REPORT("/cancel_report");

    private final String alias;

    CallBack(String alias) {
        this.alias = alias;
    }

}
