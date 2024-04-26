package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.entity.TransactionLogEntity;
import tech.nuqta.investtraderbotfiverr.enums.TransactionStatus;
import tech.nuqta.investtraderbotfiverr.repository.TransactionLogRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TelegramBot telegramBot;
    private final TransactionLogRepository transactionLogRepository;

    public void markTransactionAsSuccess(String transactionId) {
        SendMessage sendMessage = new SendMessage();
        transactionLogRepository.findByTransactionId(transactionId).ifPresent(transactionLogEntity -> {
            transactionLogEntity.setStatus(TransactionStatus.SUCCESS);
            transactionLogRepository.save(transactionLogEntity);
            sendMessage.setChatId(transactionLogEntity.getTelegramId().toString());
            sendMessage.setText("Your transaction has been successfully completed.");
            telegramBot.sendMsg(sendMessage);
        });

    }

    public void saveTransaction(TransactionLogEntity transactionLogEntity) {
        transactionLogRepository.save(transactionLogEntity);
    }
}
