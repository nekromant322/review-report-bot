package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REPORT_HISTORY;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.REPORT_HISTORY_HELP_MESSAGE;
import static com.nekromant.telegram.utils.ValidationUtils.validateArguments;

@Component
public class ReportHistoryCommand extends MentoringReviewCommand {

    @Value("${server.host}")
    private String appHost;

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
            int limitCount = strings.length > 1 ? Integer.parseInt(strings[1]) : 5;
            String studentUserName = parseUserName(strings);
            String messageWithHistory = reportRepository.findAllByStudentUserName(studentUserName)
                    .stream()
                    .sorted(Comparator.comparing(Report::getDate).reversed())
                    .limit(limitCount)
                    .sorted(Comparator.comparing(Report::getDate))
                    .map(report -> report.getStudentUserName() + "\n" + report.getDate() + "\n" + report.getHours() + "\n" +
                            report.getTitle())
                    .collect(Collectors.joining("\n-----------------\n"));

            messageWithHistory += "\n\n" + appHost + "/charts.html?student=" + studentUserName;
            message.setText(messageWithHistory);

            execute(absSender, message, user);
        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR + REPORT_HISTORY_HELP_MESSAGE);
            execute(absSender, message, user);
        }
    }

    private String parseUserName(String[] strings) {
        return strings[0].replaceAll("@", "");
    }
}
