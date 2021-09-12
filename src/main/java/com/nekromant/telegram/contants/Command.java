package com.nekromant.telegram.contants;

public enum Command {

    START("start", "Начать использование бота"),
    REGISTER_MENTOR("register_mentor", "Зарегистрировать чат как менторский"),
    REGISTER_REPORT("register_report", "Зарегистрировать чат как чат с отчетами"),
    REVIEW("review", "Попросить о ревью"),
    GET_MENTORS("get_mentors", "Узнать список менторов"),
    ADD_MENTORS("add_mentors", "Добавить менторов"),
    REPORT("report", "Отправить отчет"),
    MY_STAT("mystat", "Посмотреть свою статистику"),
    ALL_STAT("allstat", "Посмотреть статистику по всем"),
    REPORT_HISTORY("report_history", "Посмотреть историю отчетов"),
    REPORT_DELETE("report_delete", "Удалить отчеты студента");

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
