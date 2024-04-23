package tech.nuqta.investtraderbotfiverr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.service.MessageService;
import tech.nuqta.investtraderbotfiverr.service.UserService;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final MessageService messageService;
    private final MessageSource messageSource;
    private final UserService userService;
    private static final String BOT_USERNAME = "@InvestTraderBot";

    @Lazy
    @Autowired
    public TelegramBot(TelegramBotsApi telegramBotsApi, MessageService messageService, MessageSource messageSource, UserService userService) throws TelegramApiException {
        super("7019113638:AAEjBNmHpDhrHOaoyrc1w6e6NAMyULDfJpE");
        this.messageService = messageService;
        this.messageSource = messageSource;
        this.userService = userService;
        telegramBotsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            var sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            var state = userService.getUserState(message.getChatId(), message.getFrom());
            if (state == UserState.START) {
                messageService.handleStartMessage(message);
            } else {
                messageService.handleMainMessage(message);
            }

        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            var state = userService.getUserState(callbackQuery.getFrom().getId(), callbackQuery.getFrom());
            if (state == UserState.START) {
                messageService.handleLanguageCallbackQuery(callbackQuery);
            }
        }
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