package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REPORT_HISTORY;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.utils.ValidationUtils.validateArguments;

@Component
public class ReportHistoryCommand extends MentoringReviewCommand {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    public ReportHistoryCommand() {
        super(REPORT_HISTORY.getAlias(), REPORT_HISTORY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            validateArguments(strings);
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());

            String messageWithHistory = reportRepository.findAllByStudentUserName(parseUserName(strings))
                    .stream()
                    .sorted(Comparator.comparing(Report::getDate).reversed())
                    .limit(Integer.parseInt(strings[1]))
                    .sorted(Comparator.comparing(Report::getDate))
                    .map(report -> report.getStudentUserName() + "\n" + report.getDate() + "\n" + report.getHours() + "\n" +
                            report.getTitle())
                    .collect(Collectors.joining("\n"));
            message.setText(messageWithHistory);

            execute(absSender, message, user);


        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR);
            execute(absSender, message, user);
        }
    }

    private String parseUserName(String[] strings) {
        return strings[0].replaceAll("@", "");
    }
}
