package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.service.ContractService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.management.InstanceNotFoundException;
import java.time.LocalDate;

import static com.nekromant.telegram.contants.Command.SET_CONTRACT;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class SetContractCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private ContractService contractService;
    @Autowired
    private UserInfoService userInfoService;

    public SetContractCommand() {
        super(SET_CONTRACT.getAlias(), SET_CONTRACT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        String studentUserName;
        String contractId;
        LocalDate date;
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            studentUserName = arguments[0].replaceAll("@", "");
            contractId = arguments[1];
            date = LocalDate.parse(arguments[2], defaultDateFormatter());

        }  catch (Exception e) {
            message.setText("Пример: \n" +
                    "/set_contract @kyomexd 1234567890 05.05.2023\n\n"
                    + e.getMessage());
            execute(absSender, message, user);
            return;
        }
        try {
            Contract contract = contractService.getContractByUsername(studentUserName);
            contractService.updateContract(studentUserName, contractId, date);
            message.setText("Было изменено для " + studentUserName +
                    " с " + contract.getContractId() + " " + contract.getDate().format(defaultDateFormatter()) +
                    " на " + contractId + " " + date.format(defaultDateFormatter()));

        } catch (InstanceNotFoundException e) {
            contractService.saveContract(Contract.builder().username(studentUserName).studentInfo(userInfoService.getUserInfo(studentUserName)).contractId(contractId).date(date).build());
            message.setText("Заданы новые данные для " + studentUserName + " " + contractId + " " + date.format(defaultDateFormatter()));
        }
        execute(absSender, message, user);
    }
}
