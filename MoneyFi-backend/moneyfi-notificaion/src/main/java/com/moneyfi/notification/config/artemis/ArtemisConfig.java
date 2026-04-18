package com.moneyfi.notification.config.artemis;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

import static com.moneyfi.notification.util.constants.StringConstants.LOCAL_PROFILE_ARTEMIS;

@Configuration
@Profile(LOCAL_PROFILE_ARTEMIS)
public class ArtemisConfig {

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }
}
