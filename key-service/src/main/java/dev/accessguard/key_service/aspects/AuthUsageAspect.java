package dev.accessguard.key_service.aspects;

import annotations.TrackUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import logging.AbstractUsageAspect;
import models.UsageEvent;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthUsageAspect extends AbstractUsageAspect {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, UsageEvent> kafkaTemplate;


    public AuthUsageAspect(ObjectMapper objectMapper, KafkaTemplate<String, UsageEvent> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        System.out.println("Aspect instance created: " + this.hashCode());
    }

    @AfterReturning(value = "@annotation(trackUsage)", returning = "result")
    public void afterSuccess(TrackUsage trackUsage, Object result){
        process(trackUsage, result);
    }

    @Override
    protected void emit(Object payload) {
        try {
            UsageEvent event = (UsageEvent) payload;
            String json = objectMapper.writeValueAsString(event);
            System.out.println("emiting event");
            kafkaTemplate.send("USAGE-LOG",event.getTenantName(),event);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
