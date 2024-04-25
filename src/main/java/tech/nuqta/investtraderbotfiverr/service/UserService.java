package tech.nuqta.investtraderbotfiverr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import tech.nuqta.investtraderbotfiverr.enums.UserState;
import tech.nuqta.investtraderbotfiverr.repository.UserRepository;
import tech.nuqta.investtraderbotfiverr.user.UserEntity;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "state", key = "#telegramId")
    public UserState getUserState(Long telegramId, User user) {
        Optional<UserEntity> userEntityOptional = userRepository.findByTelegramId(telegramId);
        UserEntity userEntity;
        if (userEntityOptional.isPresent()) {
            userEntity = userEntityOptional.get();
        } else {
            userEntity = new UserEntity();
            userEntity.setTelegramId(user.getId());
            userEntity.setName(user.getFirstName());
            userEntity.setUsername(user.getUserName());
            userEntity.setState(UserState.START);
            userRepository.save(userEntity);
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
        UserEntity userEntity;
        if (userEntityOptional.isPresent()) {
            userEntity = userEntityOptional.get();
        } else {
            userEntity = new UserEntity();
            userEntity.setTelegramId(telegramId);
        }
        userEntity.setState(state);
        userRepository.save(userEntity);
        return userEntity;
    }

    @CacheEvict(value = "state", key = "#userEntity.getTelegramId()")
    public void saveUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

}
