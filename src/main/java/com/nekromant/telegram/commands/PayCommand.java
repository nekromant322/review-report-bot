package com.nekromant.telegram.commands;

import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.service.ContractService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.management.InstanceNotFoundException;
import static com.nekromant.telegram.contants.Command.PAY;

@Component
@Slf4j
public class PayCommand extends MentoringReviewCommand {
    @Value("${pay-info.login}")
    private String login;
    @Value("${pay-info.apikey}")
    private String apikey;
    @Value("${pay-info.method}")
    private String method;
    @Autowired
    private ContractService contractService;
    @Autowired
    private LifePayFeign lifePayFeign;
    @Autowired
    private UserInfoService userInfoService;


    public PayCommand() {
        super(PAY.getAlias(), PAY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        userInfoService.initializeUserInfo(chat, user);

        SendMessage message = new SendMessage();
        String studentChatId = chat.getId().toString();
        message.setChatId(studentChatId);

        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            ChequeDTO chequeDTO = new ChequeDTO(
                    login,
                    apikey,
                    parseAmount(arguments),
                    createDescription(user),
                    parseCustomerPhone(arguments),
                    method);

            log.info(String.valueOf(chequeDTO));
            log.info("Sending request to LifePay");
            String lifePayResponse = lifePayFeign.payCheque(chequeDTO).getBody();
            log.info("LifePay response: " + lifePayResponse);
            String paymentUrl = parseUrl(lifePayResponse);
            log.info("PaymentURL: " + paymentUrl);

            message.enableMarkdownV2(true);
            message.setText("Отправлено SMS\\-сообщение со счетом на номер " + chequeDTO.getCustomerPhone()
                    + " на сумму " + chequeDTO.getAmount() + "\n[Ссылка на оплату](" + paymentUrl + ")");

        } catch (InstanceNotFoundException e) {
            message.setText("У вас нет контракта, обратитесь к @Marandyuk_Anatolii");
            execute(absSender, message, user);
            return;
        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/pay <ваш номер> <сумма услуги>");
            execute(absSender, message, user);
            return;
        }
        execute(absSender, message, user);
    }

    private String createDescription(User user) throws InstanceNotFoundException {
        final Contract contract = contractService.getContractByUserId(user.getId());
        return "Оплата по договору " + contract.getContractId() + " от " +
                contract.getDate() + " за консультации по разработке ПО";
    }

    public String parseCustomerPhone(String[] arguments){
        return validateCustomerPhone(arguments[0]);
    }

    private String validateCustomerPhone(String phone) {
        phone = phone.replaceAll("[^0-9+]", "");
        if (phone.startsWith("7") && phone.length() == 11){
            return phone;
        } else if (phone.startsWith("+7") && phone.length() == 12) {
            return "7" + phone.substring(2);
        } else if (phone.startsWith("8") && phone.length() == 11) {
            return "7" + phone.substring(1);
        } else {
            throw new RuntimeException("Некорректный формат номера");
        }
    }

    public String parseAmount(String[] arguments){
        return validateAmount(arguments[1]);
    }

    private String validateAmount(String argument) {
        try {
            Double.parseDouble(argument);
            return argument;
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseUrl(String json) {
        return json.substring(json.lastIndexOf("https"), json.lastIndexOf("\"")).replace("\\", "");
    }

}
