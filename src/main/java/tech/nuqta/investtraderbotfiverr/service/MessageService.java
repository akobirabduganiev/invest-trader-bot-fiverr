package tech.nuqta.investtraderbotfiverr.service;

import com.paypal.base.rest.PayPalRESTException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.TransactionLogEntity;
import tech.nuqta.investtraderbotfiverr.enums.PaymentMethod;
import tech.nuqta.investtraderbotfiverr.enums.SubscriptionType;
import tech.nuqta.investtraderbotfiverr.enums.TransactionStatus;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.paypal.PaypalService;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;
import tech.nuqta.investtraderbotfiverr.utils.TelegramBotUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
    private final TransactionService transactionService;
    private final SubscriptionService subscriptionService;

    @Value("${subscription.type.monthly}")
    private String monthlySubscriptionValue;
    @Value("${subscription.type.weekly}")
    private String weeklySubscriptionValue;
    private final String serverAddress = "http://localhost:8080";

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
        userService.saveUser(userEntity);
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

        var monthlySubscriptionName = messageSource.getMessage("subscription.type.monthly", null, locale);
        var weeklySubscriptionName = messageSource.getMessage("subscription.type.monthly", null, locale);

        var subscriptionType = callbackQuery.getData().equals(monthlySubscriptionValue) ? monthlySubscriptionName : weeklySubscriptionName;

        editMessageText.setText("Choose a payment method for " + subscriptionType + " subscription");
        editMessageText.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonOneEachRow(buttonList));

        var userEntity = userService.updateUserState(user.getId(), UserState.PAYMENT_METHOD_CHOOSING);
        var subscriptionEntity = userEntity.getSubscription();

        // If a user does not have a subscription, create a new one
        if (subscriptionEntity == null) {
            subscriptionEntity = new SubscriptionEntity();
            userEntity.setSubscription(subscriptionEntity);
        }

        // Set subscription type
        if (callbackQuery.getData().equals(monthlySubscriptionValue)) {
            subscriptionEntity.setSubscriptionType(SubscriptionType.MONTHLY);
        } else {
            subscriptionEntity.setSubscriptionType(SubscriptionType.WEEKLY);
        }

        subscriptionService.saveSubscription(subscriptionEntity);
        userService.saveUser(userEntity);

        telegramBot.sendMsg(editMessageText);
    }

    public void handlePaymentMethodCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var user = callbackQuery.getFrom();
        var userEntity = userService.getUser(user.getId());
        var deleteMessage = new DeleteMessage();
        var transactionLog = new TransactionLogEntity();
        var subscriptionEntity = subscriptionService.getSubscription(userEntity);
        transactionLog.setTelegramId(user.getId());
        transactionLog.setPaymentMethod(data.equals("paypal") ? PaymentMethod.PAYPAL : PaymentMethod.STRIPE);
        transactionLog.setUser(userEntity);

        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        telegramBot.sendMsg(deleteMessage);

        var editMessageText = new EditMessageText();
        var sendMessage = new SendMessage();

        sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        sendMessage.setText("We're getting your payment ready. This may take a moment.");

        try {
            var message = telegramBot.execute(sendMessage);
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setChatId(message.getChatId().toString());

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        var euro = subscriptionEntity.getSubscriptionType() == SubscriptionType.MONTHLY ? monthlySubscriptionValue : weeklySubscriptionValue;
        double value = Double.parseDouble(euro.replace("€", "").replace(",", "."));
        transactionLog.setMessageId(editMessageText.getMessageId());
        if (transactionLog.getPaymentMethod() == PaymentMethod.PAYPAL) {
            var link = generatePaypalPaymentAndReturnApprovalUrl(value, transactionLog);
            editMessageText.setText("Please select the option below to complete your payment.");
            editMessageText.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonWithLink("Pay " + euro, link));
        } else if (transactionLog.getPaymentMethod() == PaymentMethod.STRIPE) {
            String url = generateStripeCheckoutUrl(value, transactionLog);
            editMessageText.setText("Please select the option below to complete your payment.");
            editMessageText.setReplyMarkup(TelegramBotUtils.createInlineKeyboardButtonWithLink("Pay " + euro, url));
        }
        telegramBot.sendMsg(editMessageText);
    }

    public void handleSubscribedUserMessage(Message message, SubscriptionEntity subscriptionEntity) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        LocalDateTime expiryDate = subscriptionEntity.getExpiryDate();
        var duration = Duration.between(LocalDateTime.now(), expiryDate);

        long days = duration.toDays();
        sendMessage.setText("You are already subscribed to our service. Enjoy! \uD83D\uDE0A" +
                "\n\nYour subscription will expire in " + days + " days.");
        telegramBot.sendMsg(sendMessage);

    }

    private String generatePaypalPaymentAndReturnApprovalUrl(double value, TransactionLogEntity transactionLog) {
        try {
            var payment = paypalService.createPayment(value,
                    "EUR",
                    "paypal",
                    "sale",
                    "Subscription payment",
                    serverAddress + "/payment/cancel",
                    serverAddress + "/payment/success");

            transactionLog.setAmount(value);
            transactionLog.setPaymentMethod(PaymentMethod.PAYPAL);
            transactionLog.setCurrency("EUR");
            transactionLog.setDescription("Subscription payment");
            transactionLog.setStatus(TransactionStatus.PENDING);
            transactionLog.setTransactionId(payment.getId());
            transactionService.saveTransaction(transactionLog);

            for (var links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return links.getHref();
                }
            }
            throw new RuntimeException("Approval URL not found");
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateStripeCheckoutUrl(double value, TransactionLogEntity transactionLog) {
        try {
            Stripe.apiKey = "sk_test_51P83twEuQQZGBc7JpXsATNdVQ9zY7Rho8RcRhHZgTIQ6QuPZKV32sZnoPi5Eq2Ic88O2HiX60JzzC5eF8H6iX3ZZ00T3IoU8wC";

            Map<String, Object> checkoutSessionParams = new LinkedHashMap<>();
            var cancelUrl = serverAddress + "/payment/cancel";
            checkoutSessionParams.put("cancel_url", cancelUrl);
            checkoutSessionParams.put("success_url", "https://t.me/InvestTraderBot");
            checkoutSessionParams.put("payment_method_types", Collections.singletonList("card"));
            checkoutSessionParams.put("mode", "payment");

            Map<String, Object> lineItem = new LinkedHashMap<>();
            Map<String, Object> priceData = new LinkedHashMap<>();

            priceData.put("currency", "eur");
            priceData.put("unit_amount", (long) value * 100);
            priceData.put("product_data", Map.of("name", "Subscription payment"));
            lineItem.put("price_data", priceData);
            lineItem.put("quantity", 1);

            checkoutSessionParams.put("line_items", Collections.singletonList(lineItem));

            var session = Session.create(checkoutSessionParams);
            transactionLog.setPaymentMethod(PaymentMethod.STRIPE);
            transactionLog.setAmount(value);
            transactionLog.setCurrency("EUR");
            transactionLog.setDescription("Subscription payment");
            transactionLog.setStatus(TransactionStatus.PENDING);
            transactionLog.setTransactionId(session.getId());
            transactionService.saveTransaction(transactionLog);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
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
