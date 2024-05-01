package com.nekromant.telegram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.PaymentDetailsDTO;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.model.*;
import com.nekromant.telegram.service.PaymentDetailsService;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


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
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    ResumeAnalysisRequestService resumeAnalysisRequestService;

    @PostMapping(value = "/paymentCallback")

    public void paymentCallback(@RequestParam("data") String json) throws JsonProcessingException {
        PaymentDetailsDTO paymentDetailsDTO = objectMapper.readValue(json, PaymentDetailsDTO.class);
        PaymentDetails paymentDetails = modelMapper.map(paymentDetailsDTO, PaymentDetails.class);
        sendMessage(paymentDetails);

        var pendingPay = paymentDetailsService.findByNumber(paymentDetails.getNumber());
        if (pendingPay!= null && pendingPay.getStatus().equals(PayStatus.UNREDEEMED.get())) {
            resumeAnalysisRequestService.sendCVToMentorForAnalysisOrReject(paymentDetails);
            return;
        }

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