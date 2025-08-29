package com.nekromant.telegram.contants;

public enum Command {

    START("start", "Начать использование бота"),
    REGISTER_MENTOR("register_mentor", "Зарегистрировать чат как менторский"),
    REGISTER_REPORT("register_report", "Зарегистрировать чат как чат с отчетами"),
    REVIEW("review", "Попросить о ревью"),
    GET_MENTORS("get_mentors", "Узнать список менторов"),
    ADD_MENTOR("add_mentor", "Добавить ментора"),
    DELETE_MENTOR("delete_mentor", "Удалить ментора"),
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
    ANNOUNCE("announce", "Отправить аннонс о новых возможностях указанным пользователям"),
    PAY("pay", "Оплатить чек"),
    GET_CONTRACTS("get_contracts", "Посмотреть все контракты"),
    DAILY("daily", "Назначить ежедневное уведомление"),
    DAILY_DELETE("daily_delete", "Удалить ежедневное уведомление"),
    ENABLE_NOTIFICATION("/add_reminder", "Включить уведеомленя о платежах для пользователей"),
    DISABLE_NOTIFICATION("/remove_reminder", "Выключить уведеомленя о платежах для пользователей"),
    NOTIFY_REPORT_ON("/report_reminder_on", "Включить уведеомленя о пользователях, ненаписавших отчет"),
    NOTIFY_REPORT_OFF("/report_reminder_off", "Выключить уведеомленя о платежах для пользователей"),
    SCHEDULE_REVIEW("/schedule", "Расписание всех ревью");

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
