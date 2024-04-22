package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;
import tech.nuqta.investtraderbotfiverr.user.UserEntity;
import tech.nuqta.investtraderbotfiverr.utils.TelegramBotUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.nuqta.investtraderbotfiverr.enums.UserState.LANGUAGE_CHOOSING;

@Component
@RequiredArgsConstructor
public class MessageService {
    @Lazy
    private final TelegramBot telegramBot;
    private final UserRepository userRepository;
    public static final HashMap<Long, UserState> userState = new HashMap<>();

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

    public void handleCallbackMessage(CallbackQuery callbackQuery) {
        var user = findByTelegramId(callbackQuery.getFrom().getId());

        if (user == null) {
            user = new UserEntity();
            user.setTelegramId(callbackQuery.getFrom().getId());
            user.setName(callbackQuery.getFrom().getFirstName());
            user.setUsername(callbackQuery.getFrom().getUserName());
        }
        userState.put(callbackQuery.getFrom().getId(), LANGUAGE_CHOOSING);
        var language = callbackQuery.getData();
        user.setLanguage(language);
        userRepository.save(user);
        System.out.println("User " + user.getName() + " has been saved");
    }

    private UserEntity findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElse(null);
    }
}
