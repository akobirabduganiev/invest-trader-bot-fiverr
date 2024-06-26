package tech.nuqta.investtraderbotfiverr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableCaching
public class InvestTraderBotFiverrApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestTraderBotFiverrApplication.class, args);
    }

}
