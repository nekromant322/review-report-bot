package com.nekromant.telegram.contants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CallBack {
    APPROVE_REVIEW_REQUEST("approveReviewRequest"),
    DENY_REVIEW_REQUEST("denyReviewRequest"),
    SET_REPORT_DATE_TIME("setReportDateTime"),
    DENY_REPORT_DATE_TIME("denyReportDateTime"),
    SET_REVIEW_REQUEST_DATE_TIME("reviewRequestDateTime"),
    DENY_REVIEW_REQUEST_DATE_TIME("reviewRequestDenyDateTime");

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
