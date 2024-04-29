package tech.nuqta.investtraderbotfiverr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserService userService;
    private static final String BOT_USERNAME = "@InvestTraderBot";
    @Value("${subscription.type.monthly}")
    private String monthlySubscriptionValue;
    @Value("${subscription.type.weekly}")
    private String weeklySubscriptionValue;

    @Lazy
    @Autowired
    public TelegramBot(TelegramBotsApi telegramBotsApi, MessageService messageService, UserService userService) throws TelegramApiException {
        super("7019113638:AAEjBNmHpDhrHOaoyrc1w6e6NAMyULDfJpE");
        this.messageService = messageService;
        this.userService = userService;
        telegramBotsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            var message = update.getMessage();
            var state = userService.getUserState(message.getChatId());
            var subscription = userService.getUser(message.getChatId()).getSubscription();
            var isBot = message.getFrom().getIsBot();
            if (message.getChatId() < 0) {
                return;
            }
            if (!isBot) {
                if (subscription != null && subscription.getIsActive().equals(true)) {
                    messageService.handleSubscribedUserMessage(message, subscription);
                    return;
                }
                if (state == UserState.START) {
                    messageService.handleStartMessage(message);
                } else {
                    messageService.handleMainMessage(message);
                }
            }

        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            var state = userService.getUserState(callbackQuery.getFrom().getId());
            if (state == UserState.START) {
                messageService.handleLanguageCallbackQuery(callbackQuery);
            }

            if (callbackQuery.getData().equals(monthlySubscriptionValue) || callbackQuery.getData().equals(weeklySubscriptionValue)) {
                messageService.handleSubscriptionCallbackQuery(callbackQuery);
            } else if (callbackQuery.getData().equals("paypal") || callbackQuery.getData().equals("stripe")) {
                messageService.handlePaymentMethodCallbackQuery(callbackQuery);
            } else if (callbackQuery.getData().equals("back")) {
                messageService.handleBackCallbackQuery(callbackQuery);

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