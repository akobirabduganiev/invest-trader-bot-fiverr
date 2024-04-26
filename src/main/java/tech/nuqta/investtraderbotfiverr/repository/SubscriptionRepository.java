package tech.nuqta.investtraderbotfiverr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.nuqta.investtraderbotfiverr.entity.SubscriptionEntity;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findByUser(UserEntity user);
}