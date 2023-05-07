package com.nekromant.telegram.commands;

import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.model.Cheque;
import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.service.ContractService;
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

import static com.nekromant.telegram.contents.Command.PAY;

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
    public PayCommand() {
        super(PAY.getAlias(), PAY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        String studentChatId = chat.getId().toString();
        message.setChatId(studentChatId);

        try {
            ValidationUtils.validateArguments(strings);
            Cheque cheque = new Cheque(
                    login,
                    apikey,
                    parseAmount(strings),
                    createDescription(user),
                    parseCustomerPhone(strings),
                    method);

            log.info(lifePayFeign.payCheque(cheque).getBody());

            message.setText("Отправлено SMS-сообщение со счетом на номер " + cheque.getCustomerPhone()
                    + " на сумму " + cheque.getAmount());

        } catch (InstanceNotFoundException e) {
            message.setText("У вас нет контракта");
            execute(absSender, message, user);
            return;
        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/pay 79775548911 5000.00");
            execute(absSender, message, user);
            return;
        }
        execute(absSender, message, user);
    }

    private String createDescription(User user) throws InstanceNotFoundException {
        final Contract contract = contractService.getContractByUsername(user.getUserName());
        return "Оплата по договору " + contract.getContractId() + " от " +
                contract.getDate() + " за консультации по разработке ПО";
    }

    public String parseCustomerPhone(String[] strings){
        return strings[0];
    }


    public String parseAmount(String[] strings){
        return validateAmount(strings[1]);
    }

    private String validateAmount(String string) {
        try {
            Double.parseDouble(string);
            return string;
        } catch (ClassCastException e) {
            return null;
        }
    }
}
