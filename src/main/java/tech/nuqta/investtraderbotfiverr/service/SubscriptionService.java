package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public void activateSubscription(UserEntity user, SubscriptionEntity subscription) {
        subscription.setIsActive(true);
        if (subscription.getSubscriptionType().equals(SubscriptionType.MONTHLY))
            subscription.setExpiryDate(LocalDateTime.now().plusMonths(1));
        else
            subscription.setExpiryDate(LocalDateTime.now().plusWeeks(1));

        createOrUpdateSubscription(user, subscription);

    }

    public SubscriptionEntity getSubscription(UserEntity user) {
        return subscriptionRepository.findByUser(user).orElse(new SubscriptionEntity());
    }

/*    public void cancelSubscription(UserEntity user) {
        Optional<SubscriptionEntity> subscription = subscriptionRepository.findByUser(user);
        subscription.ifPresent(subscriptionEntity -> {
            subscriptionEntity.setIsActive(false);
            subscriptionRepository.save(subscriptionEntity);
        });
    }*/

    public void saveSubscription(SubscriptionEntity subscriptionEntity) {
        subscriptionRepository.save(subscriptionEntity);
    }
}
