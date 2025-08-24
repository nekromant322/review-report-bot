package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.ReportService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REPORT_DELETE;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.REPORTS_DELETED;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Slf4j
@Component
public class ReportDeleteCommand extends OwnerCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ReportService reportService;

    public ReportDeleteCommand() {
        super(REPORT_DELETE.getAlias(), REPORT_DELETE.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            validateArgumentsNumber(strings);
            SendMessage message = sendMessageFactory.create(chat);
            reportService.deleteByUserName(strings[0].replaceAll("@", ""));
            message.setText(REPORTS_DELETED);
            execute(absSender, message, user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SendMessage message = sendMessageFactory.create(chat);
            message.setText(ERROR + "/report_delete @anfisa_andrienko");
            execute(absSender, message, user);
        }
    }
}
