package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;
import tech.nuqta.investtraderbotfiverr.repository.SubscriptionRepository;

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
            entity.setRemainingDays(subscription.getRemainingDays());
            subscriptionRepository.save(entity);
        } else {
            subscription.setUser(user);
            subscriptionRepository.save(subscription);
        }

    }

    public SubscriptionEntity getSubscription(UserEntity user) {
        return subscriptionRepository.findByUser(user).orElse(new SubscriptionEntity());
    }

    public void cancelSubscription(UserEntity user) {
        Optional<SubscriptionEntity> subscription = subscriptionRepository.findByUser(user);
        subscription.ifPresent(subscriptionEntity -> {
            subscriptionEntity.setIsActive(false);
            subscriptionRepository.save(subscriptionEntity);
        });
    }

    public void saveSubscription(SubscriptionEntity subscriptionEntity) {
        subscriptionRepository.save(subscriptionEntity);
    }
}
