package com.nekromant.telegram.controller;

import com.google.gson.Gson;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.OrderDTO;
import com.nekromant.telegram.commands.dto.PaymentDetailsDTO;
import com.nekromant.telegram.commands.dto.PurchaseDTO;
import com.nekromant.telegram.model.*;
import com.nekromant.telegram.service.PaymentDetailsService;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;


@RestController
@Slf4j
public class PaymentDetailsRestController {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/paymentCallback")
    public void paymentCallback(@RequestBody PaymentDetailsDTO paymentDetailsDTO) {
        PaymentDetails paymentDetails = modelMapper.map(paymentDetailsDTO, PaymentDetails.class);
        sendMessage(paymentDetails);
        paymentDetailsService.save(paymentDetails);
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
                .append("Имя плательщика: ").append(paymentDetails.getCardHolder()).append("\n")
                .append("Дата транзакции: ").append(paymentDetails.getCreated()).append("\n").toString();
    }
}