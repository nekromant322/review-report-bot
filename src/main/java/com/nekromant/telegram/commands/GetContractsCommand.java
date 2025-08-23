package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.service.ContractService;
import com.nekromant.telegram.utils.SendMessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.GET_CONTRACTS;

@Component
public class GetContractsCommand extends OwnerCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ContractService contractService;
    static final String DELIMETER = "\n";

    public GetContractsCommand() {
        super(GET_CONTRACTS.getAlias(), GET_CONTRACTS.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = sendMessageFactory.create(chat);
        message.setText(createMessageText());
        execute(absSender, message, user);
    }

    private String createMessageText() {
        return contractService.getAllContracts().stream()
                .map(contract ->
                        getUserName(contract) + " | " + contract.getContractId() + " | " + contract.getDate())
                .collect(Collectors.joining(DELIMETER));
    }

    private String getUserName(Contract contract) {
        return contract.getStudentInfo() == null ? "Пользователя нет в БД" : "@" + contract.getStudentInfo().getUserName();
    }
}
