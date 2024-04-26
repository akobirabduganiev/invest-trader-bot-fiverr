package tech.nuqta.investtraderbotfiverr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByTelegramId(Long telegramId);
}