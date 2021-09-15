package com.nekromant.telegram.commands;


import com.nekromant.telegram.contants.Step;
import com.nekromant.telegram.model.StepPassed;
import com.nekromant.telegram.repository.StepPassedRepository;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDate;

import static com.nekromant.telegram.contants.Command.STEP_PASSED;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class StepPassedCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private StepPassedRepository stepPassedRepository;

    @Autowired
    public StepPassedCommand() {
        super(STEP_PASSED.getAlias(), STEP_PASSED.getDescription());
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

            StepPassed stepPassed = StepPassed.builder()
                    .id(null)
                    .studentUserName(arguments[0].replaceAll("@", ""))
                    .step(Step.getStepByAlias(arguments[1]))
                    .date(parseDate(arguments))
                    .build();
            stepPassedRepository.save(stepPassed);
            message.setText("Шаг " + stepPassed.getStep().getAlias() + " пройден для студента @" + stepPassed.getStudentUserName());

        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/step_passed @anfisa_andrienko begin 09.04.2020\n\n"
                    + e.getMessage());
            execute(absSender, message, user);
        }
        execute(absSender, message, user);
    }

    private LocalDate parseDate(String[] strings) {

        return LocalDate.parse(strings[2], defaultDateFormatter());
    }
}
