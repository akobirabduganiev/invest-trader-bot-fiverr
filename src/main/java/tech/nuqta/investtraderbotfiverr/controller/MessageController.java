package tech.nuqta.investtraderbotfiverr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.utils.TelegramBotUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageController {
    @Lazy
    private final TelegramBot telegramBot;

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
}
