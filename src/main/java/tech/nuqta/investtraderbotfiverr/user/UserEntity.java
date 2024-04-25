package tech.nuqta.investtraderbotfiverr.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tech.nuqta.investtraderbotfiverr.enums.SubscriptionType;
import tech.nuqta.investtraderbotfiverr.enums.UserState;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(unique = true)
    private Long telegramId;
    private String name;
    private String username;
    @Enumerated(EnumType.STRING)
    private UserState state;
    private String language;
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime updatedAt;


}