package com.nekromant.telegram.contants;

public enum Command {

    START("start", "Начать использование бота"),
    REGISTER("register", "Зарегистрировать чат как менторский"),
    REVIEW("review", "Попросить о ревью"),
    GET_MENTORS("get_mentors", "Узнать список менторов"),
    ADD_MENTORS("add_mentors", "Добавить менторов");

    private String alias;
    private String description;

    Command(String alias, String description) {
        this.alias = alias;
        this.description = description;
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }
}
