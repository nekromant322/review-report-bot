package com.nekromant.telegram.controller;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.*;
import com.nekromant.telegram.service.PaymentDetailsService;
import com.nekromant.telegram.service.UserInfoService;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PaymentDetailsController {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @PostMapping("/paymentCallback")
    public int paymentCallback(@RequestBody PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);
        sendMessage(paymentDetails);
        return HttpStatus.SC_OK;
    }

    public void sendMessage(PaymentDetails paymentDetails) {
        String messageText = createMessageText(paymentDetails);
        mentoringReviewBot.sendMessage(userInfoService.getUserInfo(ownerUserName).getChatId().toString(), messageText);
    }

    private String createMessageText(PaymentDetails paymentDetails) {
        return new StringBuilder("Номер заказа: ").append(paymentDetails.getNumber()).append("\n")
                .append("Статус транзакции: ").append(paymentDetails.getStatus()).append("\n")
                .append("Сумма: ").append(paymentDetails.getAmount()).append("\n")
                .append("Номер телефона плательщика: ").append(paymentDetails.getPhone()).append("\n")
                .append("Имя плательщика: ").append(paymentDetails.getCardHolder()).append("\n").toString();
    }
}