package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.service.ContractService;
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
import java.time.format.DateTimeFormatter;

import static com.nekromant.telegram.contants.Command.SET_CONTRACT;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

@Component
public class SetContractCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private ContractService contractService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public SetContractCommand() {
        super(SET_CONTRACT.getAlias(), SET_CONTRACT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        try {
            ValidationUtils.validateArguments(arguments);
            String studentUserName = arguments[0].replaceAll("@", "");
            String contractId = arguments[1];
            LocalDate date = LocalDate.parse(arguments[2], dateFormatter);
            Contract contract = contractService.getContractByUsername(studentUserName);
            contractService.updateContract(studentUserName, contractId, date);
            message.setText("Было изменено для " + studentUserName +
                    " с " + contract.getContractId() + " " + contract.getDate().format(dateFormatter) +
                    " на " + contractId + " " + date.format(dateFormatter));

        } catch (InstanceNotFoundException e) {
            Contract contract = new Contract();
            String studentUserName = arguments[0].replaceAll("@", "");
            String contractId = arguments[1];
            LocalDate date = LocalDate.parse(arguments[2], dateFormatter);
            contract.setUsername(studentUserName);
            contract.setContractId(contractId);
            contract.setDate(date);
            contractService.saveContract(contract);
            message.setText("Заданы новые данные для " + studentUserName + " " + contractId + " " + date.format(dateFormatter));

        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/set_contract @kyomexd 1234567890 05.05.2023\n\n"
                    + e.getMessage());
            execute(absSender, message, user);
            return;
        }
        execute(absSender, message, user);
    }
}
