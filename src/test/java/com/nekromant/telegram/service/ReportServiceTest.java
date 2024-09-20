package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @InjectMocks
    private ReportService reportService;
    @Mock
    private ReportRepository reportRepository;

    private static final String studentUserName0 = "nickname";
    private static final String studentUserName1 = "@Nickname";

    @ParameterizedTest
    @MethodSource("provideReportListArguments")
    void getUserStats_DoesntThrowException(List<Report> reportList) {
        // Given - arrange
        given(reportRepository.findAllByStudentUserNameIgnoreCase(anyString())).willReturn(reportList);

        // When - act
        // Then - assert/verify
        Assertions.assertDoesNotThrow(() -> reportService.getUserStats(studentUserName0));
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