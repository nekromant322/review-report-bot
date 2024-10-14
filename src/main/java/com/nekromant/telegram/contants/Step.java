package com.nekromant.telegram.contants;

import java.util.Arrays;

public enum Step {
    BEGIN("begin"),
    CORE("core"),
    WEB("web"),
    PREPROJECT("preproject"),
    PROJECT("project"),
    INTERVIEW("interview"),
    JOB("job");

    private String alias;

    Step(String alias) {
        this.alias = alias;
    }


    public String getAlias() {
        return alias;
    }

    public static Step getStepByAlias(String alias) {
        return Arrays.stream(Step.values())
                .filter(step -> step.alias.equalsIgnoreCase(alias))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Неверный шаг"));
    }
}
