package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.contants.MessageContants;
import com.nekromant.telegram.model.NotificationPay;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.*;
import com.nekromant.telegram.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationPayRepository notificationPayRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final ContractRepository contractRepository;
    private final SendMessageService sendMessageService;

    @Value("${price.mentoring-subscription}")
    private String valueSubscription;

    @Scheduled(cron = "0 0 13 L * ?")
    private void sendNotificationPay() {
        init();
    }

    private void init() {
        List<NotificationPay> notificationPayList = notificationPayRepository.getNotificationPayByEnable(true);
        List<UserInfo> userList = new ArrayList<>();

        for (NotificationPay notification : notificationPayList) {
            userList.add(notification.getUserInfo());
        }

        List<PaymentDetails> paymentList = getPaymentsForCurrentMonth();
        for (PaymentDetails pay : paymentList) {
            if (hasContractNumber(pay.getDescription()) && Objects.equals(pay.getAmount(), valueSubscription)) {
                userList.remove(contractRepository.findContractByContractId(getContractId(pay)).orElseThrow(() -> new RuntimeException("Contract not found")).getStudentInfo());
            }
        }

        sendNotification(userList);
    }

    private void sendNotification(List<UserInfo> userInfoList) {
        for (UserInfo user : userInfoList) {
            sendMessageService.sendMessage(user.getChatId().toString(), MessageContants.NOTIFICATION_FOR_USERS);
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
