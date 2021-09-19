package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.exception.TooManyReportsException;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REPORT;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.REPORT_HELP_MESSAGE;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static com.nekromant.telegram.utils.ValidationUtils.validateArguments;

@Component
public class ReportCommand extends MentoringReviewCommand {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    private UserInfoService userInfoService;


    public ReportCommand() {
        super(REPORT.getAlias(), REPORT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            validateArguments(strings);

            //тут убрать когда получу все chatId, оставить только в /start
            userInfoService.updateUserInfo(chat, user);

            ValidationUtils.validateArguments(strings);
            Report report = new Report();
            report.setDate(parseDate(strings));
            report.setHours(parseHours(strings));
            report.setStudentUserName(user.getUserName());
            report.setTitle(parseTitle(strings));

            if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
                throw new TooManyReportsException();
            }
            reportRepository.save(report);

            SendMessage message = new SendMessage();
            message.setChatId(specialChatService.getReportsChatId());
            message.setText(
                    "@" + report.getStudentUserName() + "\n" + report.getDate().format(defaultDateFormatter()) + "\n" + report.getHours() +
                            "\n" + report.getTitle());
            execute(absSender, message, user);
        } catch (TooManyReportsException exception) {
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR + exception.getMessage());
            execute(absSender, message, user);
        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR + REPORT_HELP_MESSAGE);
            execute(absSender, message, user);
        }
    }

    private String parseTitle(String[] strings) {
        return Arrays.stream(strings).skip(2).collect(Collectors.joining(" "));
    }

    private Integer parseHours(String[] strings) {
        return Integer.parseInt(strings[1]);
    }

    private LocalDate parseDate(String[] arguments) {
        if (arguments[0].equalsIgnoreCase("сегодня")) {
            return LocalDate.now(ZoneId.of("Europe/Moscow"));
        }
        if (arguments[0].equalsIgnoreCase("вчера")) {
            return LocalDate.now(ZoneId.of("Europe/Moscow")).minus(1, ChronoUnit.DAYS);
        }
        throw new InvalidParameterException();
    }

}
