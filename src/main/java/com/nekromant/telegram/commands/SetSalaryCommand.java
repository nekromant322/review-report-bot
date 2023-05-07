package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.Salary;
import com.nekromant.telegram.repository.SalaryRepository;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDate;

import static com.nekromant.telegram.contents.Command.SET_SALARY;
import static com.nekromant.telegram.contents.MessageContents.NOT_OWNER_ERROR;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class SetSalaryCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    public SetSalaryCommand() {
        super(SET_SALARY.getAlias(), SET_SALARY.getDescription());
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

            Salary salary = Salary.builder()
                    .id(null)
                    .userName(arguments[0].replaceAll("@", ""))
                    .salary(Integer.parseInt(arguments[1]))
                    .date(parseDate(arguments))
                    .build();
            salaryRepository.save(salary);
            message.setText("Новая зарплата добавлена");

        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/set_salary @vladdosiik 191000 07.10.2021\n\n"
                    + e.getMessage());
            execute(absSender, message, user);
            return;
        }
        execute(absSender, message, user);
    }

    private LocalDate parseDate(String[] strings) {

        return LocalDate.parse(strings[2], defaultDateFormatter());
    }
}
