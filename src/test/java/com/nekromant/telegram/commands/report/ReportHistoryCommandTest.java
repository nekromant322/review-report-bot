package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.model.Report;
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

    private static final String studentUserName0 = "nickname";
    private static final String studentUserName1 = "@Nickname";

    private final String commandStudentName = studentUserName0;
    private final String commandReportLimit = "20";
    private final String[] strings = new String[]{commandStudentName, commandReportLimit};

    @ParameterizedTest
    @MethodSource("provideReportListArguments")
    void executeWithReportList_DoesntShowError(List<Report> reportList) {
        // Given - arrange
        given(reportRepository.findAllByStudentUserNameIgnoreCase(anyString())).willReturn(reportList);
        given(sendMessageFactory.create(anyString(), anyString())).willReturn(new SendMessage());

        // When - act
        reportHistoryCommand.execute(absSender, user, chat, strings);

        // Then - assert/verify
        Mockito.verify(sendMessageFactory, never()).create(anyString(), contains(ERROR));
    }

    private static Stream<Arguments> provideReportListArguments() {
        List<Report> reportListWithoutNulls = new ArrayList<>();
        reportListWithoutNulls.add(new Report(0L, studentUserName1, 2, LocalDate.now(), "title"));

        List<Report> reportListWithNullStudentName = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullStudentName.add(new Report(4L, null, 2, LocalDate.now(), "title"));

        List<Report> reportListWithNullHours = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullHours.add(new Report(5L, studentUserName1, null, LocalDate.now(), "title"));

        List<Report> reportListWithNullDate = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullDate.add(new Report(9L, studentUserName1, 2, null, "title"));

        List<Report> reportListWithNullTitle = new ArrayList<>(reportListWithoutNulls);
        reportListWithNullTitle.add(new Report(13L, studentUserName1, 2, LocalDate.now(), null));


        return Stream.of(
                Arguments.of(reportListWithoutNulls),
                Arguments.of(reportListWithNullStudentName),
                Arguments.of(reportListWithNullHours),
                Arguments.of(reportListWithNullDate),
                Arguments.of(reportListWithNullTitle)
        );
    }
}