package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;
import tech.nuqta.investtraderbotfiverr.enums.SubscriptionType;
import tech.nuqta.investtraderbotfiverr.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final TelegramBot telegramBot;
    @Value("${subscription.group.id}")
    private Long groupId;


    public void createOrUpdateSubscription(UserEntity user, SubscriptionEntity subscription) {
        Optional<SubscriptionEntity> subscriptionEntity = subscriptionRepository.findByUser(user);
        if (subscriptionEntity.isPresent()) {
            SubscriptionEntity entity = subscriptionEntity.get();
            entity.setSubscriptionType(subscription.getSubscriptionType());
            entity.setExpiryDate(subscription.getExpiryDate());
            entity.setIsActive(subscription.getIsActive());
            subscriptionRepository.save(entity);
        } else {
            subscription.setUser(user);
            subscriptionRepository.save(subscription);
        }

    }

    /**
     * Activates a subscription for a user.
     *
     * @param user         the user for whom the subscription is being activated
     * @param subscription the subscription to be activated
     */
    public void activateSubscription(UserEntity user, SubscriptionEntity subscription) {
        subscription.setIsActive(true);
        if (subscription.getSubscriptionType().equals(SubscriptionType.MONTHLY))
            subscription.setExpiryDate(LocalDateTime.now().plusMonths(1));
        else
            subscription.setExpiryDate(LocalDateTime.now().plusWeeks(1));

        createOrUpdateSubscription(user, subscription);

        var createLink = new CreateChatInviteLink();
        createLink.setChatId(groupId);
        createLink.setMemberLimit(1);
        try {
            var chatLink = telegramBot.execute(createLink);
            var sendMessage = new SendMessage();
            sendMessage.setChatId(user.getTelegramId().toString());
            sendMessage.setText("Your subscription has been activated. Join the group using the link below\n" + chatLink.getInviteLink());
            telegramBot.sendMsg(sendMessage);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SubscriptionEntity getSubscription(UserEntity user) {
        return subscriptionRepository.findByUser(user).orElse(new SubscriptionEntity());
    }


    public void saveSubscription(SubscriptionEntity subscriptionEntity) {
        subscriptionRepository.save(subscriptionEntity);
    }
}
