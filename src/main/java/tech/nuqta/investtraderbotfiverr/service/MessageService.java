package tech.nuqta.investtraderbotfiverr.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.paypal.PaypalService;
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
    private final PaypalService paypalService;

    @Value("${subscription.type.monthly}")
    private String monthlySubscriptionValue;
    @Value("${subscription.type.weekly}")
    private String weeklySubscriptionValue;

    public void handleStartMessage(Message message) {
        var buttonList = createLanguageButtonList();
        var sendMessage = createSendMessage(message.getChatId().toString(), "Welcome to Invest Trader Bot, Choose a language:", buttonList);
        telegramBot.sendMsg(sendMessage);
    }

    public void handleMainMessage(Message message) {
        var fromUser = message.getFrom();

        var userEntity = userService.getUser(message.getChatId());
        var locale = new Locale(userEntity.getLanguage());
        var buttonList = createSubscriptionButtonList(locale, fromUser.getFirstName());
        var welcomeMessage = messageSource.getMessage("welcome.message", new Object[]{fromUser.getFirstName()}, locale);
        var sendMessage = createSendMessage(message.getChatId().toString(), welcomeMessage, buttonList);
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
        buttonList.add(Map.entry("Premium\uD83D\uDC8E: " + monthlySubscriptionValue + " / " + monthlySubscriptionName, monthlySubscriptionValue));
        buttonList.add(Map.entry("Premium\uD83D\uDC8E: " + weeklySubscriptionValue + " / " + weeklySubscriptionName, weeklySubscriptionValue));
        var welcomeMessage = messageSource.getMessage("welcome.message", new Object[]{user.getFirstName()}, locale);

        sendMessage.setText(welcomeMessage);
        sendMessage.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));
        telegramBot.sendMsg(deleteMessage);
        telegramBot.sendMsg(sendMessage);
        userRepository.save(userEntity);
    }


    public void handleSubscriptionCallbackQuery(CallbackQuery callbackQuery) {
        var editMessageText = new EditMessageText();
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("Paypal", "paypal"));
        buttonList.add(Map.entry("Stripe", "stripe"));
        var user = callbackQuery.getFrom();
        var locale = new Locale(userRepository.findByTelegramId(user.getId()).get().getLanguage());
        String monthlySubscriptionName = messageSource.getMessage("subscription.type.monthly", null, locale);
        String weeklySubscriptionName = messageSource.getMessage("subscription.type.monthly", null, locale);
        var subscriptionType = callbackQuery.getData().equals(monthlySubscriptionValue) ? monthlySubscriptionName : weeklySubscriptionName;
        editMessageText.setText("Choose a payment method for " + subscriptionType + " subscription");
        editMessageText.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));
        userService.updateUserState(user.getId(), UserState.PAYMENT_METHOD_CHOOSING);
        telegramBot.sendMsg(editMessageText);
    }

    public void handlePaymentMethodCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        var user = callbackQuery.getFrom();
        var locale = new Locale(userRepository.findByTelegramId(user.getId()).get().getLanguage());
        var sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        sendMessage.setText("You have chosen " + data + " as payment method");
        try {
            Payment payment = paypalService.createPayment(10.0, "USD", data, "sale", "description",
                    "http://localhost:8080/payment/cancel", "http://localhost:8080/payment/success");

            String approvalLink = payment.getLinks()
                    .stream()
                    .filter(link -> link.getRel().equalsIgnoreCase("approval_url"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Approval link not found"))
                    .getHref();

            sendMessage.setText("To continue the payment process please follow the link: " + approvalLink);

        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }
        telegramBot.sendMsg(sendMessage);
    }

    private List<Map.Entry<String, String>> createLanguageButtonList() {
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("English \uD83C\uDDEC\uD83C\uDDE7", "en"));
        buttonList.add(Map.entry("French \uD83C\uDDEB\uD83C\uDDF7", "fr"));
        return buttonList;
    }

    private List<Map.Entry<String, String>> createSubscriptionButtonList(Locale locale, String firstName) {
        var monthlySubscriptionName = messageSource.getMessage("subscription.type.monthly", new Object[]{firstName}, locale);
        var weeklySubscriptionName = messageSource.getMessage("subscription.type.weekly", new Object[]{firstName}, locale);
        List<Map.Entry<String, String>> buttonList = new ArrayList<>();
        buttonList.add(Map.entry("Premium\uD83D\uDC8E: " + monthlySubscriptionValue + " / " + monthlySubscriptionName, monthlySubscriptionValue));
        buttonList.add(Map.entry("Premium\uD83D\uDC8E: " + weeklySubscriptionValue + " / " + weeklySubscriptionName, weeklySubscriptionValue));
        return buttonList;
    }

    private SendMessage createSendMessage(String chatId, String text, List<Map.Entry<String, String>> buttonList) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));
        return sendMessage;
    }
}
