package com.nekromant.telegram.commands;

import com.nekromant.telegram.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.GET_CONTRACTS;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

@Component
public class GetContractsCommand extends MentoringReviewCommand {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private ContractService contractService;
    static final String DELIMETER = "\n";

    public GetContractsCommand() {
        super(GET_CONTRACTS.getAlias(), GET_CONTRACTS.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }

        String messageText = createMessageText();
        message.setText(messageText);

        execute(absSender, message, user);
    }

    private String createMessageText() {
        return contractService.getAllContracts().stream()
                .map(contract ->
                        "@" + contract.getUsername() + " | " + contract.getContractId() + " | " + contract.getDate())
                .collect(Collectors.joining(DELIMETER));
    }
}
