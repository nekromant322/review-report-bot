package com.nekromant.telegram.contants;

public enum PayStatus {
    SUCCESS("success"),
    UNREDEEMED("unredeemed"),
    FAIL("fail");

    private String alias;

    PayStatus(String alias) {
        this.alias = alias;
    }

    public String get() {
        return alias;
    }

}
