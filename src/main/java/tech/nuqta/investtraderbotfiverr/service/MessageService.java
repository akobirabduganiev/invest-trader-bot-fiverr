package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;
import tech.nuqta.investtraderbotfiverr.utils.TelegramBotUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static tech.nuqta.investtraderbotfiverr.enums.UserState.LANGUAGE_CHOOSING;

@Service
@RequiredArgsConstructor
public class MessageService {
    @Lazy
    private final TelegramBot telegramBot;
    private final UserService userService;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    @Value("${subscription.type.monthly}")
    private String monthlySubscriptionValue;
    @Value("${subscription.type.weekly}")
    private String weeklySubscriptionValue;

    public void handleStartMessage(Message message) {
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("English \uD83C\uDDEC\uD83C\uDDE7", "en"));
        buttonList.add(Map.entry("French \uD83C\uDDEB\uD83C\uDDF7", "fr"));
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Welcome to Invest Trader Bot, Choose a language:");
        sendMessage.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButton(buttonList));
        telegramBot.sendMsg(sendMessage);
    }

    public void handleMainMessage(Message message) {
        var user = message.getFrom();
        var entity = userService.getUser(message.getChatId());
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        var locale = new Locale(entity.getLanguage());
        var monthlySubscriptionName = messageSource.getMessage("subscription.type.monthly", new Object[]{user.getFirstName()}, locale);
        var weeklySubscriptionName = messageSource.getMessage("subscription.type.weekly", new Object[]{user.getFirstName()}, locale);
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("Premium: " + monthlySubscriptionValue + " / " + monthlySubscriptionName, monthlySubscriptionValue));
        buttonList.add(Map.entry("Premium: " + weeklySubscriptionValue + " / " + weeklySubscriptionName, weeklySubscriptionName));
        var welcomeMessage = messageSource.getMessage("welcome.message", new Object[]{user.getFirstName()}, locale);

        sendMessage.setText(welcomeMessage);
        sendMessage.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));
        telegramBot.sendMsg(sendMessage);
    }

    public void handleLanguageCallbackQuery(CallbackQuery callbackQuery) {
        var user = callbackQuery.getFrom();
        var deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());

        var userEntity = userService.updateUserState(user.getId(), LANGUAGE_CHOOSING);
        userEntity.setLanguage(callbackQuery.getData());
        var sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        var locale = new Locale(callbackQuery.getData());
        var monthlySubscriptionName = messageSource.getMessage("subscription.type.monthly", new Object[]{user.getFirstName()}, locale);
        var weeklySubscriptionName = messageSource.getMessage("subscription.type.weekly", new Object[]{user.getFirstName()}, locale);
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("Premium: " + monthlySubscriptionValue + " / " + monthlySubscriptionName, monthlySubscriptionValue));
        buttonList.add(Map.entry("Premium: " + weeklySubscriptionValue + " / " + weeklySubscriptionName, weeklySubscriptionName));
        var welcomeMessage = messageSource.getMessage("welcome.message", new Object[]{user.getFirstName()}, locale);

        sendMessage.setText(welcomeMessage);
        sendMessage.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));
        telegramBot.sendMsg(deleteMessage);
        telegramBot.sendMsg(sendMessage);
        userRepository.save(userEntity);
    }


}
