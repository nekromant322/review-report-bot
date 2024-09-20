package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.exception.TooManyReportsException;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
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
import static com.nekromant.telegram.contants.MessageContants.*;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Slf4j
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
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            sendAnswer(chat.getId().toString(), GROUP_CHAT_IS_NOT_SUPPORTED, absSender, user);
        } else {
            try {

                validateArgumentsNumber(strings);

                //тут убрать когда получу все chatId, оставить только в /start
                userInfoService.initializeUserInfo(chat, user);

                ValidationUtils.validateArgumentsNumber(strings);
                Report report = new Report();
                report.setDate(parseDate(strings));
                report.setHours(parseHours(strings));
                report.setStudentUserName(user.getUserName());
                report.setTitle(parseTitle(strings));

                if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
                    throw new TooManyReportsException();
                }
                reportRepository.save(report);

                sendAnswer(specialChatService.getReportsChatId(), "@" + report.getStudentUserName() + "\n" + report.getDate().format(defaultDateFormatter()) + "\n" + report.getHours() +
                        "\n" + report.getTitle(), absSender, user);
            } catch (TooManyReportsException exception) {
                sendAnswer(chat.getId().toString(), ERROR + exception.getMessage(), absSender, user);
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
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(absSender, message, user);
    }

    private String parseTitle(String[] strings) {
        return Arrays.stream(strings).skip(2).collect(Collectors.joining(" "));
    }

    private Integer parseHours(String[] strings) {
        int hours = Integer.parseInt(strings[1]);
        validateHoursArgument(hours);
        return hours;
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

    private void validateHoursArgument(int hours) {
        if (hours < 0 || hours > 24) {
            throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 24");
        }
    }
}
