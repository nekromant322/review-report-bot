package com.nekromant.telegram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekromant.telegram.commands.dto.PaymentDetailsDTO;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.service.*;
import com.nekromant.telegram.model.PaymentDetails;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    ClientPaymentRequestServiceProvider paymentRequestServiceProvider;

    private ClientPaymentRequestService clientPaymentRequestService;
    @Autowired
    private SendMessageService sendMessageService;

    @PostMapping(value = "/paymentCallback")

    public void paymentCallback(@RequestParam("data") String json) throws JsonProcessingException {
        Converter<String, PayStatus> stringPayStatusConverter = new AbstractConverter<String, PayStatus>() {
            @Override
            public PayStatus convert(String status) {
                return PayStatus.valueOf(status.toUpperCase());
            }
        };
        modelMapper.addConverter(stringPayStatusConverter);

        PaymentDetailsDTO paymentDetailsDTO = objectMapper.readValue(json, PaymentDetailsDTO.class);
        PaymentDetails paymentDetails = modelMapper.map(paymentDetailsDTO, PaymentDetails.class);
        sendMessage(paymentDetails);

        PaymentDetails pendingPay = paymentDetailsService.findByNumber(paymentDetails.getNumber());
        if (pendingPay != null && pendingPay.getStatus() != PayStatus.SUCCESS) {
            paymentDetails.setServiceType(pendingPay.getServiceType());
            clientPaymentRequestService = paymentRequestServiceProvider.getClientPaymentRequestService(pendingPay.getServiceType());
            if (paymentDetails.getStatus() == PayStatus.FAIL) {
                clientPaymentRequestService.rejectApplication(paymentDetails);
                return;
            }
            clientPaymentRequestService.notifyMentor(paymentDetails);
            return;
        }

        paymentDetailsService.save(paymentDetails);
    }

    public void sendMessage(PaymentDetails paymentDetails) {
        String messageText = createMessageText(paymentDetails);
        sendMessageService.sendMessage(userInfoService.getUserInfo(ownerUserName).getChatId().toString(), messageText);
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