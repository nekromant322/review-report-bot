package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.utils.SendMessageFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportHistoryCommandTest {
    @InjectMocks
    private ReportHistoryCommand reportHistoryCommand;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private AbsSender absSender;
    @Mock
    private User user;
    @Mock
    private Chat chat;
    @Mock
    private SendMessageFactory sendMessageFactory;

    private static final String studentUserName1 = "@Nickname";

    private final String commandStudentName = "nickname";
    private final String commandReportLimit = "20";
    private final String[] strings = new String[]{commandStudentName, commandReportLimit};
    private static final UserInfo USER_INFO = UserInfo.builder().chatId(0L).userName(studentUserName1).build();

    @ParameterizedTest
    @MethodSource("provideReportListArguments")
    void executeWithReportList_DoesntShowError(List<Report> reportList) {
        // Given - arrange
        given(reportRepository.findAllByUserInfo_UserNameIgnoreCase(anyString())).willReturn(reportList);
        given(sendMessageFactory.create(anyString(), anyString())).willReturn(new SendMessage());

        // When - act
        reportHistoryCommand.execute(absSender, user, chat, strings);

        // Then - assert/verify
        Mockito.verify(sendMessageFactory, never()).create(anyString(), contains(ERROR));
    }

    private static Stream<Arguments> provideReportListArguments() {
        List<Report> reportListWithoutNulls = new ArrayList<>();
        reportListWithoutNulls.add(new Report(0L, USER_INFO, 2, LocalDate.now(), "title"));

        List<Report> reportListWithNullHours = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullHours.add(new Report(1L, USER_INFO, null, LocalDate.now(), "title"));

        List<Report> reportListWithNullDate = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullDate.add(new Report(2L, USER_INFO, 2, null, "title"));

        List<Report> reportListWithNullTitle = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullTitle.add(new Report(3L, USER_INFO, 2, LocalDate.now(), null));

        List<Report> reportListWithNullStudentName = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullStudentName.add(new Report(4L, UserInfo.builder().userName(null).build(), 2, LocalDate.now(), "title"));


        return Stream.of(
                Arguments.of(reportListWithoutNulls),
                Arguments.of(reportListWithNullStudentName),
                Arguments.of(reportListWithNullHours),
                Arguments.of(reportListWithNullDate),
                Arguments.of(reportListWithNullTitle)
        );
    }
}