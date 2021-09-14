package com.nekromant.telegram.contants;

public class MessageContants {

    public static final String ERROR = "Что-то пошло не так\n";

    public static final String REPORT_HELP_MESSAGE = "Чтобы отправить отчет \n/report [сегодня|вчера] <кол-во часов> <чем занимался>\n" +
            "Пример:\n /report сегодня 2 Читал статьи про ооп\n\n";
    public static final String REPORT_HISTORY_HELP_MESSAGE = "Чтобы получить историю отчетов\n/" + Command.REPORT_HISTORY.getAlias() + " " +
            "@username <кол-во отчетов>";
    public static final String REVIEW_HELP_MESSAGE = "Для того чтобы попросить " +
            "ревью напишите " +
            "что-то вроде \n/" + Command.REVIEW.getAlias() + " [сегодня|завтра] <таймслоты через запятую> Тема:<Тема ревью>\n" +
            "Пример:\n/" + Command.REVIEW.getAlias() + " завтра 15 16 20 Тема: 4 модуль";
    public static final String START_HELP_MESSAGE = REPORT_HELP_MESSAGE + REVIEW_HELP_MESSAGE;
    public static final String UNKNOWN_COMMAND = "Не понимаю команду";
    public static final String REVIEW_BOOKED = "Ревью c @%s назначено на \n%s\n%s";
    public static final String REVIEW_APPROVED = "@%s апрувнул ревью с @%s\n %s";
    public static final String NOBODY_CAN_MAKE_REVIEW = "Попробуй выбрать другое время\nНикто не может провести ревью %s\n";
    public static final String SOMEBODY_DENIED_REVIEW = "@%s отменил ревью с @%s";
    public static final String REVIEW_REQUEST_SENT = "Запрос отправлен менторам, ответ скоро придет";
    public static final String REVIEW_INCOMING = "Скоро ревью у @%s с @%s\n%s\n%s";
    public static final String USER_STAT_MESSAGE =
            "@%s\nВсего дней - %s\nУчился дней - %s\nУчился часов - %s\nВ среднем в неделю - %s часов";

    public static final String REPORTS_DELETED = "Отчеты удалены";
    public static final String NOW_OWNER_ERROR = "Ты не владелец бота";
    public static final String REPORT_REMINDER = "Не забудь написать отчет \uD83D\uDE4A \n";
}
