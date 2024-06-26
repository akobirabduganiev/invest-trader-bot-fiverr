package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import tech.nuqta.investtraderbotfiverr.entity.UserEntity;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "state", key = "#telegramId")
    public UserState getUserState(Long telegramId) {
        Optional<UserEntity> userEntityOptional = userRepository.findByTelegramId(telegramId);
        UserEntity userEntity;
        if (userEntityOptional.isPresent()) {
            userEntity = userEntityOptional.get();
        } else {
            return UserState.START;
        }
        return userEntity.getState();
    }

    public UserEntity getUser(Long telegramId) {
        Optional<UserEntity> userEntityOptional = userRepository.findByTelegramId(telegramId);
        return userEntityOptional.orElse(new UserEntity());
    }

    @CacheEvict(value = "state", key = "#telegramId")
    public UserEntity updateUserState(Long telegramId, UserState state) {
        Optional<UserEntity> userEntityOptional = userRepository.findByTelegramId(telegramId);
        UserEntity entity;
        if (userEntityOptional.isPresent()) {
            entity = userEntityOptional.get();
        } else {
            entity = new UserEntity();
            entity.setTelegramId(telegramId);
        }
        entity.setState(state);
        userRepository.save(entity);
        return entity;
    }

    @CacheEvict(value = "state", key = "#userEntity.getTelegramId()")
    public void saveUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public List<UserEntity> findAllWithActiveSubscriptions() {
        return userRepository.findAllWithActiveSubscriptions();
    }
}
