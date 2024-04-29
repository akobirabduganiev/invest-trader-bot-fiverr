package tech.nuqta.investtraderbotfiverr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByTelegramId(Long telegramId);

  @Query("SELECT u from UserEntity u WHERE u.subscription.isActive = true")
  List<UserEntity> findAllWithActiveSubscriptions();
}