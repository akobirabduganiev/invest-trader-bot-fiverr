package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;
import tech.nuqta.investtraderbotfiverr.utils.TelegramBotUtils;

import java.nio.charset.StandardCharsets;
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

    public void handleLanguageCallbackQuery(CallbackQuery callbackQuery) {
        User user = callbackQuery.getFrom();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());

        var userEntity = userService.updateUserState(user.getId(), LANGUAGE_CHOOSING);
        userEntity.setLanguage(callbackQuery.getData());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        Locale locale = new Locale(callbackQuery.getData());

        var welcomeMessage = messageSource.getMessage("welcome.message", new Object[]{user.getFirstName()}, locale);

        byte[] bytes = welcomeMessage.getBytes(StandardCharsets.UTF_8);
        sendMessage.setText(new String(bytes, StandardCharsets.UTF_8));
        telegramBot.sendMsg(deleteMessage);
        telegramBot.sendMsg(sendMessage);
        userRepository.save(userEntity);
    }


}
