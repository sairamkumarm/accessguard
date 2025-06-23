package dev.accessguard.notification_service;

import models.UsageEvent;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class kafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsageEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UsageEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UsageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }


    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }
}
