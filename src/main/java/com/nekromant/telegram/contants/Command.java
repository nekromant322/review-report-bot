package com.nekromant.telegram.contants;

public enum Command {

    START("start", "Начать использование бота"),
    REGISTER_MENTOR("register_mentor", "Зарегистрировать чат как менторский"),
    REGISTER_REPORT("register_report", "Зарегистрировать чат как чат с отчетами"),
    REVIEW("review", "Попросить о ревью"),
    GET_MENTORS("get_mentors", "Узнать список менторов"),
    ADD_MENTOR("add_mentor", "Добавить ментора"),
    REPORT("report", "Отправить отчет"),
    MY_STAT("mystat", "Посмотреть свою статистику"),
    ALL_STAT("allstat", "Посмотреть статистику по всем"),
    REPORT_HISTORY("report_history", "Посмотреть историю отчетов"),
    REPORT_DELETE("report_delete", "Удалить отчеты студента"),
    MY_OFF("me_off", "Стать пассивным ментором"),
    MY_ON("me_on", "Стать активным ментором"),
    STEP_PASSED("step_passed", "Отметить для студента пройденный шаг обучения"),
    REVIEW_TODAY("review_today", "Расписание ревью на сегодня"),
    SET_SALARY("set_salary", "Установить новое значение зп"),
    NOTIFY_REVIEW_ON("notify_review_on", "Уведолять о всех ревью"),
    NOTIFY_REVIEW_OFF("notify_review_off", "Не уведолять о всех ревью"),
    SET_SCHEDULE_PERIOD("set_schedule_period", "Установить период времени, за который выводить список ревью"),
    SET_CONTRACT("set_contract", "Задать номер договора студента"),
    PAY("pay", "Оплатить чек");
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
