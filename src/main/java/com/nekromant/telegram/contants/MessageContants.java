package com.nekromant.telegram.contants;

public class MessageContants {

    public static final String ERROR = "Что-то пошло не так\n";
    public static final String START_MESSAGE = "Добро пожаловать в бота для бронирования ревью!\nДля того чтобы попросить ревью напишите " +
            "что-то вроде \n/review 26.05.2021 15 17 18 Тема: 5 модуль\n15 17 18 - таймслоты\nслово \"Тема\" обязательно \n\nЧтобы узнать" +
            " список менторов, которые сейчас могут провести ревью " + Command.GET_MENTORS.getAlias();
    public static final String UNKNOWN_COMMAND = "Не понимаю команду";
    public static final String REVIEW_BOOKED = "Ревью c @%s назначено на \n%s\n%s";
    public static final String REVIEW_APPROVED = "@%s апрувнул ревью с @%s\n %s";
    public static final String NOBODY_CAN_MAKE_REVIEW = "Попробуй выбрать другое время\nНикто не может провести ревью %s\n";
    public static final String SOMEBODY_DENIED_REVIEW = "@%s отменил ревью с @%s";
    public static final String REVIEW_REQUEST_SENT = "Запрос отправлен менторам, ответ скоро придет";
}
