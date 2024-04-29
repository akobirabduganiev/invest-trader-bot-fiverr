package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.nuqta.investtraderbotfiverr.config.TelegramBot;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;
import tech.nuqta.investtraderbotfiverr.enums.SubscriptionType;
import tech.nuqta.investtraderbotfiverr.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final TelegramBotsApi telegramBotsApi;
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
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 12 * * ?")  // runs daily at 12pm
    public void handleSubscriptions() {
        List<UserEntity> users = userService.findAllWithActiveSubscriptions();

        for (UserEntity user : users) {
            LocalDateTime expiryDate = user.getSubscription().getExpiryDate();
            long daysToExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);

            if (daysToExpiry <= 0) {
                removeUserFromChannel(groupId, user.getTelegramId());
                deactivateSubscription(user.getSubscription());
                var sendMessage = new SendMessage();
                sendMessage.setChatId(user.getTelegramId().toString());
                sendMessage.setText("""
                        Your subscription has expired. Please renew to continue receiving signals
                        
                        Use the command "/start" to renew your subscription
                        """);
                telegramBot.sendMsg(sendMessage);
            }
        }
    }

    void deactivateSubscription(SubscriptionEntity subscription) {
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }

    public void removeUserFromChannel(Long groupId, Long userId) {
        var banChatMember = new BanChatMember(groupId.toString(), userId);
        var unbanChatMember = new UnbanChatMember(groupId.toString(), userId);

        try {
            telegramBot.execute(banChatMember);
            telegramBot.execute(unbanChatMember);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public SubscriptionEntity getSubscription(UserEntity user) {
        return subscriptionRepository.findByUser(user).orElse(new SubscriptionEntity());
    }


    public void saveSubscription(SubscriptionEntity subscriptionEntity) {
        subscriptionRepository.save(subscriptionEntity);
    }
}
