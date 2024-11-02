package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REPORT_DELETE;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;
import static com.nekromant.telegram.contants.MessageContants.REPORTS_DELETED;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Slf4j
@Component
public class ReportDeleteCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private ReportRepository reportRepository;

    public ReportDeleteCommand() {
        super(REPORT_DELETE.getAlias(), REPORT_DELETE.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            validateArgumentsNumber(strings);
            SendMessage message = new SendMessage();
            String chatId = chat.getId().toString();
            message.setChatId(chatId);

            if (!user.getUserName().equals(ownerUserName)) {
                message.setText(NOT_OWNER_ERROR);
                execute(absSender, message, user);
                return;
            }
            reportRepository.deleteByUserInfo_UserNameIgnoreCase(strings[0].replaceAll("@", ""));
            message.setText(REPORTS_DELETED);
            execute(absSender, message, user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR + "/report_delete @anfisa_andrienko");
            execute(absSender, message, user);
        }
    }
}
