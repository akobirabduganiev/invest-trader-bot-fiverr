package tech.nuqta.investtraderbotfiverr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tech.nuqta.investtraderbotfiverr.enums.SubscriptionType;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;
    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL)
    private UserEntity user;
    private LocalDateTime expiryDate;
    private Boolean isActive = false;
}