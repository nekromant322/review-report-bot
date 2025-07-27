package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.contants.MessageContants;
import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.model.NotificationPay;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.*;
import com.nekromant.telegram.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationPayRepository notificationPayRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final ContractRepository contractRepository;
    private final SendMessageService sendMessageService;
    private final UserInfoRepository userInfoRepository;

    @Value("${owner.userName}")
    private String ownerUserName;

    @Scheduled(cron = "0 0 13 L * ?")
    private void sendNotificationPay() {
        log.info("Ежемесячное уведомление пользователей не оплативших подписку");
        initSendNotification();
    }


    private void initSendNotification() {
        List<NotificationPay> notificationPayList = notificationPayRepository.getNotificationPayByEnable(true);
        List<UserInfo> userList = new ArrayList<>();

        for (NotificationPay notification : notificationPayList) {
            if(notification.isEnable()){
                userList.add(notification.getUserInfo());
            }
        }

        List<PaymentDetails> paymentList = getPaymentsForCurrentMonth();
        paymentList.stream()
                .filter(pay -> hasContractNumber(pay.getDescription()))
                .map(pay -> contractRepository.findContractByContractId(getContractId(pay))
                        .orElseThrow(() -> new RuntimeException("Contract not found")))
                .map(Contract::getStudentInfo)
                .forEach(userList::remove);


        sendNotification(userList);
        sendNotificationOwner(userList);
    }

    private void sendNotification(List<UserInfo> userInfoList) {
        for (UserInfo user : userInfoList) {
            sendMessageService.sendMessage(user.getChatId().toString(), MessageContants.NOTIFICATION_FOR_USERS);
        }
    }

    private void sendNotificationOwner(List<UserInfo> userInfoList) {
        if (!userInfoList.isEmpty()) {
            StringBuilder textForMentor = new StringBuilder();
            textForMentor.append("Пользователи которые не оплатили подписку: \n");
            for (UserInfo user : userInfoList) {
                textForMentor.append("@" + user.getUserName() + "\n");
            }
            Long ownerChatId = userInfoRepository.findUserInfoByUserName(ownerUserName).getChatId();
            sendMessageService.sendMessage(ownerChatId.toString(), textForMentor.toString());
        }
    }

    private String getContractId(PaymentDetails pay) {
        String description = pay.getDescription();
        Matcher matcher = Pattern.compile("\\b(\\d+)\\b").matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "none";
    }

    public boolean hasContractNumber(String description) {
        if (description == null) {
            return false;
        }
        Matcher matcher = Pattern.compile("договору\\s+(\\d+)").matcher(description);
        return matcher.find();
    }

    public List<PaymentDetails> getPaymentsForCurrentMonth() {
        YearMonth current = YearMonth.now();

        String start = current.atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();

        String end = current.plusMonths(1)
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();

        return paymentDetailsRepository.findAllByCreatedAtBetween(start, end);
    }
}
