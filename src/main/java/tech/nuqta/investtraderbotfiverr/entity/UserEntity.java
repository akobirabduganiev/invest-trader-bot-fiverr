package tech.nuqta.investtraderbotfiverr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tech.nuqta.investtraderbotfiverr.enums.UserState;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity extends BaseEntity {
    @Column(unique = true)
    private Long telegramId;
    private String name;
    private String username;
    @Enumerated(EnumType.STRING)
    private UserState state;
    private String language;
    @OneToOne(fetch = FetchType.EAGER)
    private SubscriptionEntity subscription;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TransactionLogEntity> transactions;

}