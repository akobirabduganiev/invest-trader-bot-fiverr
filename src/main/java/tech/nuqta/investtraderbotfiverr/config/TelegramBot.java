package tech.nuqta.investtraderbotfiverr.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.nuqta.investtraderbotfiverr.service.MessageService;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final MessageService messageService;
    private static final String START_COMMAND = "/start";
    private static final String BOT_USERNAME = "@InvestTraderBot";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.equals(START_COMMAND)) {
                messageService.handleStartMessage(update.getMessage());
            }
        } else if (update.hasCallbackQuery()) {
            messageService.handleCallbackMessage(update.getCallbackQuery());
        }
    }

    @Autowired
    @Lazy
    public TelegramBot(TelegramBotsApi telegramBotsApi, MessageService messageService) throws TelegramApiException {
        super("7019113638:AAEjBNmHpDhrHOaoyrc1w6e6NAMyULDfJpE");
        this.messageService = messageService;
        telegramBotsApi.registerBot(this);
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public void sendMsg(Object obj) {
        try {
            handleExecution(obj);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleExecution(Object obj) throws TelegramApiException {
        if (obj instanceof SendMessage) {
            this.execute((SendMessage) obj);
        } else if (obj instanceof SendPhoto) {
            this.execute((SendPhoto) obj);
        } else if (obj instanceof SendVideo) {
            this.execute((SendVideo) obj);
        } else if (obj instanceof SendLocation) {
            this.execute((SendLocation) obj);
        } else if (obj instanceof SendVoice) {
            this.execute((SendVoice) obj);
        } else if (obj instanceof SendContact) {
            this.execute((SendContact) obj);
        } else if (obj instanceof EditMessageText) {
            this.execute((EditMessageText) obj);
        } else if (obj instanceof SendDocument) {
            this.execute((SendDocument) obj);
        } else if (obj instanceof DeleteMessage) {
            this.execute((DeleteMessage) obj);
        }
    }
}