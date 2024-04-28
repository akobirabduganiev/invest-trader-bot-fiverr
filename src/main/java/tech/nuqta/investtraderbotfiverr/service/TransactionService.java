package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.TransactionLogEntity;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;
import tech.nuqta.investtraderbotfiverr.enums.TransactionStatus;
import tech.nuqta.investtraderbotfiverr.repository.TransactionLogRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TelegramBot telegramBot;
    private final TransactionLogRepository transactionLogRepository;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public void markTransactionAsSuccess(String transactionId) {
        var sendMessage = new SendMessage();
        transactionLogRepository.findByTransactionId(transactionId).ifPresent(transactionLogEntity -> {
            var deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(transactionLogEntity.getTelegramId().toString());
            deleteMessage.setMessageId(transactionLogEntity.getMessageId());
            telegramBot.sendMsg(deleteMessage);

            transactionLogEntity.setStatus(TransactionStatus.SUCCESS);
            transactionLogRepository.save(transactionLogEntity);
            sendMessage.setChatId(transactionLogEntity.getTelegramId().toString());
            sendMessage.setText("Your transaction has been successfully completed.");
            telegramBot.sendMsg(sendMessage);
            UserEntity user = userService.getUser(transactionLogEntity.getTelegramId());
            SubscriptionEntity subscription = user.getSubscription();
            subscriptionService.activateSubscription(user, subscription);

        });

    }

    public void saveTransaction(TransactionLogEntity transactionLogEntity) {
        transactionLogRepository.save(transactionLogEntity);
    }

}
