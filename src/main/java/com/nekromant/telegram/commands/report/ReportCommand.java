package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewWithMessageIdCommand;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;

import static com.nekromant.telegram.contants.Command.REPORT;
import static com.nekromant.telegram.contants.MessageContants.*;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Slf4j
@Component
public class ReportCommand extends MentoringReviewWithMessageIdCommand {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ReportDateTimePicker reportDateTimePicker;

    public ReportCommand() {
        super(REPORT.getAlias(), REPORT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, Integer messageId, String[] strings) {
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            sendAnswer(chat.getId().toString(), GROUP_CHAT_IS_NOT_SUPPORTED, absSender, user);
        } else {
            try {

                validateArgumentsNumber(strings);

                //тут убрать когда получу все chatId, оставить только в /start
                userInfoService.initializeUserInfo(chat, user);

                Report report = Report.getTemporaryReport(strings, user.getUserName());

                reportRepository.save(report);
                absSender.execute(reportDateTimePicker.getDatePickerSendMessage(user.getId().toString(), report, messageId));
            } catch (InvalidParameterException e) {
                log.error(e.getMessage(), e);
                sendAnswer(chat.getId().toString(), e.getMessage() + "\n" + REPORT_HELP_MESSAGE, absSender, user);
            } catch (NumberFormatException e) {
                log.error("Часы должны быть указаны целым числом. {}", e.getMessage());
                sendAnswer(chat.getId().toString(), "Часы должны быть указаны целым числом\n" + REPORT_HELP_MESSAGE, absSender, user);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendAnswer(chat.getId().toString(), ERROR + REPORT_HELP_MESSAGE, absSender, user);
            }
        }
    }

    private void sendAnswer(String chatId, String text, AbsSender absSender, User user) {
        SendMessage message = sendMessageFactory.create(chatId, text);
        execute(absSender, message, user);
    }
}
