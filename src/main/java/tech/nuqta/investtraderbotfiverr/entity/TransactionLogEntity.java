package tech.nuqta.investtraderbotfiverr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tech.nuqta.investtraderbotfiverr.enums.PaymentMethod;
import tech.nuqta.investtraderbotfiverr.enums.TransactionStatus;

@Getter
@Setter
@Entity
@Table(name = "transaction_logs")
public class TransactionLogEntity extends BaseEntity {
    private Long telegramId;
    private Integer messageId;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private Double amount;
    private String currency;
    private String description;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}