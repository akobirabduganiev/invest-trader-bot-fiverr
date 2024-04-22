package tech.nuqta.investtraderbotfiverr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class BeansConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable("Your Auditor");
    }
}
