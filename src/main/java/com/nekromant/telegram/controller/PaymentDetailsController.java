package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.*;
import com.nekromant.telegram.service.PaymentDetailsService;
import com.nekromant.telegram.service.UserInfoService;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;


@RestController
public class PaymentDetailsController {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/paymentCallback")
    public int paymentCallback(@RequestBody PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);

        sendMessage(paymentDetails);

        return HttpStatus.SC_OK;
    }

    public void sendMessage(PaymentDetails paymentDetails) {
        String messageText = createMessageText(paymentDetails);
        SendMessage message = new SendMessage(
                userInfoService.getUserInfo(ownerUserName).getChatId().toString(),
                messageText
        );

    }

    private String createMessageText(PaymentDetails paymentDetails) {
        Order order = paymentDetails.getOrder();

        return "";
    }
}
