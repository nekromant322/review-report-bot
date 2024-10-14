package com.nekromant.telegram;

import com.nekromant.telegram.commands.report.ReportDateTimePicker;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.ReportService;
import com.nekromant.telegram.service.update_handler.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.utils.SendMessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.nekromant.telegram.contants.Command.REPORT;

@ExtendWith(MockitoExtension.class)
class MentoringReviewBotTest {
    @InjectMocks
    private MentoringReviewBot mentoringReviewBot;
    @Mock
    private Update update;
    @Mock
    private Map<CallBack, CallbackStrategy> callbackStrategyMap;
    @Mock
    private List<CallbackStrategy> callbackStrategies;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ReportService reportService;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReportDateTimePicker reportDateTimePicker;
    @Mock
    private SendMessageFactory sendMessageFactory;
    @Mock
    private AbsSender absSender;

    @BeforeEach
    public void setUp() {
        // нужно т.к. Мокито не умеет работать с миксованными инжектами (у нас в оригинальном классе через поля и конструктор)
        MockitoAnnotations.initMocks(this);
    }

//    @ParameterizedTest
//    @MethodSource("provideArguments")
//    void processEditedMessageUpdate(Message message) throws TelegramApiException {
//        // Given - arrange
//        given(update.getMessage()).willReturn(null);
//        given(update.getCallbackQuery()).willReturn(null);
//        given(update.hasCallbackQuery()).willReturn(false);
//        given(update.hasMessage()).willReturn(false);
//        given(update.hasEditedMessage()).willReturn(true);
//        given(update.getEditedMessage()).willReturn(message);
//        given(chatMessageRepository.findByUserMessageId(anyInt())).willReturn(new ChatMessage());
//        given(sendMessageFactory.create(anyString(), anyString())).willCallRealMethod();
////        given(mentoringReviewBot.execute(sendMessageFactory.create("1", "test тест"))).willReturn(message);
//        MentoringReviewBot spy = Mockito.spy(mentoringReviewBot);
//        doReturn(message).when(spy).execute(sendMessageFactory.create("1", "test тест"));
//
//        // When - act
//        mentoringReviewBot.processEditedMessageUpdate(update);
//
//        // Then - assert/verify
////        Assertions.assertDoesNotThrow(() -> mentoringReviewBot.processEditedMessageUpdate(update));
////        Mockito.verify(mentoringReviewBot, times(1)).processEditedMessageUpdate(update);
//    }

    private static Stream<Arguments> provideArguments() {
        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setUserName("username");

        Message reportCommandMessage = new Message();
        reportCommandMessage.setMessageId(1);
        reportCommandMessage.setText("/" + REPORT.getAlias() + " 3 test тест");
        reportCommandMessage.setChat(chat);
        reportCommandMessage.setFrom(user);

        Message unrecognizedMessage = new Message();
        unrecognizedMessage.setMessageId(1);
        unrecognizedMessage.setText("test тест");
        unrecognizedMessage.setChat(chat);
        unrecognizedMessage.setFrom(user);

        return Stream.of(
                Arguments.of(reportCommandMessage)
//                Arguments.of(unrecognizedMessage) // StackOverflow
        );
    }
}