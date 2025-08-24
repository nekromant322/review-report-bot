package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.ContractService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.SendMessageFactory;
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
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class SetContractCommand extends OwnerCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ContractService contractService;
    @Autowired
    private UserInfoService userInfoService;

    @Value("${bot.name}")
    private String botName;

    public SetContractCommand() {
        super(SET_CONTRACT.getAlias(), SET_CONTRACT.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        String studentUserName;
        String contractId;
        LocalDate date;
        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            studentUserName = arguments[0].replaceAll("@", "");
            contractId = arguments[1];
            date = LocalDate.parse(arguments[2], defaultDateFormatter());

        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/set_contract @kyomexd 1234567890 05.05.2023\n\n"
                    + e.getMessage());
            execute(absSender, message, user);
            return;
        }
        try {
            Contract contract = contractService.getContractByUsername(studentUserName);
            contractService.updateContractByUsername(studentUserName, contractId, date);
            message.setText("Было изменено для " + studentUserName +
                    " с " + contract.getContractId() + " " + contract.getDate().format(defaultDateFormatter()) +
                    " на " + contractId + " " + date.format(defaultDateFormatter()));

        } catch (InstanceNotFoundException e) {
            UserInfo userInfo = userInfoService.getUserInfo(studentUserName);
            if (userInfo == null) {
                message.setText("Попросите пользователя @" + studentUserName + " пройти инициализацию в боте @" + botName);
            } else {
                contractService.saveContract(userInfo, contractId, date);
                message.setText("Создан новый контракт для " + studentUserName + " " + contractId + " " + date.format(defaultDateFormatter()));
            }
        }
        execute(absSender, message, user);
    }
}
