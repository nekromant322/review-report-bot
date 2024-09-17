package com.nekromant.telegram.commands.stat;

import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.service.ActualStatPhotoHolderService;
import com.nekromant.telegram.service.ReportService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AllStatCommandTest {
    @InjectMocks
    private AllStatCommand allStatCommand;
    @Mock
    private ReportService reportService;
    @Mock
    private ActualStatPhotoHolderService photoHolderService;
    @Mock
    private SendMessageFactory sendMessageFactory;
    @Mock
    private SendMessage mockMessage;
    @Mock
    private AbsSender absSender;
    @Mock
    private User user;
    @Mock
    private Chat chat;

    private static final String studentUserName0 = "nickname";
    private static final String studentUserName1 = "@Nickname";

    private final String commandStudentName = studentUserName0;
    private final String[] strings = new String[]{commandStudentName};


    @ParameterizedTest
    @MethodSource("provideReportListArguments")
    void executeUserStatisticList_DoesntShowError(List<UserStatistic> userStatisticList) {
        // Given - arrange
        Mockito.when(reportService.getAllUsersStats()).thenReturn(userStatisticList);
        Mockito.when(sendMessageFactory.create(anyString(), anyString())).thenReturn(new SendMessage());
        Mockito.when(photoHolderService.getEncodedPerDayGraph()).thenReturn("encodedGraph");

        // When - act
        allStatCommand.execute(absSender, user, chat, strings);

        // Then - assert/verify
        Mockito.verify(mockMessage, never()).setText(contains(ERROR));
    }

    private static Stream<Arguments> provideReportListArguments() {
        List<UserStatistic> userStatisticListWithoutNulls = new ArrayList<>();
        userStatisticListWithoutNulls.add(UserStatistic.builder().userName(studentUserName1).totalDays(28).totalHours(120).studyDays(20).averagePerWeek(30).build());

        List<UserStatistic> userStatisticListWithNullStudentName = new ArrayList<>(userStatisticListWithoutNulls);
        userStatisticListWithNullStudentName.add(UserStatistic.builder().userName(null).totalDays(28).totalHours(120).studyDays(20).averagePerWeek(30).build());


        List<UserStatistic> userStatisticListWithZeroTotalHours = new ArrayList<>(userStatisticListWithoutNulls);
        userStatisticListWithZeroTotalHours.add(UserStatistic.builder().userName(studentUserName1).totalDays(28).totalHours(0).studyDays(20).averagePerWeek(30).build());

        List<UserStatistic> userStatisticListWithZeroStudyDays = new ArrayList<>(userStatisticListWithoutNulls);
        userStatisticListWithZeroStudyDays.add(UserStatistic.builder().userName(studentUserName1).totalDays(28).totalHours(120).studyDays(0).averagePerWeek(30).build());

        List<UserStatistic> userStatisticListWithZeroAveragePerWeek = new ArrayList<>(userStatisticListWithoutNulls);
        userStatisticListWithZeroAveragePerWeek.add(UserStatistic.builder().userName(studentUserName1).totalDays(28).totalHours(120).studyDays(20).averagePerWeek(0).build());


        return Stream.of(
                Arguments.of(userStatisticListWithoutNulls),
                Arguments.of(userStatisticListWithNullStudentName),
                Arguments.of(userStatisticListWithZeroTotalHours),
                Arguments.of(userStatisticListWithZeroStudyDays),
                Arguments.of(userStatisticListWithZeroAveragePerWeek)
        );
    }
}