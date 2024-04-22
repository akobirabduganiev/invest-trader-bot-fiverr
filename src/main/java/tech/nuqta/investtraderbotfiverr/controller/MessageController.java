package tech.nuqta.investtraderbotfiverr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;

@Component
@RequiredArgsConstructor
public class MessageController {
    @Lazy
    private final TelegramBot telegramBot;

    public void handleStartMessage(Message message) {

    }
}
