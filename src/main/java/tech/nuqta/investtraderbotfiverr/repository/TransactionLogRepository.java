package tech.nuqta.investtraderbotfiverr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.nuqta.investtraderbotfiverr.entity.TransactionLogEntity;

import java.util.Optional;

public interface TransactionLogRepository extends JpaRepository<TransactionLogEntity, Long> {

    Optional<TransactionLogEntity> findByTransactionId(String transactionId);
}