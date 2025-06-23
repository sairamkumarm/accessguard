package dev.accessguard.usage_service.services;

import models.MessageEvent;
import models.UsageEvent;
import org.apache.hc.core5.http.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class UsageService {
    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;
    private final LogPusher logPusher;

    @Value("${MAIL_ID}")
    private String mailID;

    public UsageService(KafkaTemplate<String, MessageEvent> kafkaTemplate, LogPusher logPusher) {
        this.kafkaTemplate = kafkaTemplate;
        this.logPusher = logPusher;
    }

    @KafkaListener(topics = "USAGE-LOG", groupId = "usage-consumer-group")
    public void usageLogger(UsageEvent event, Acknowledgment acknowledgment) {
        System.out.println(event.toString());
        try {
            logPusher.indexUsage(event); // might throw
            acknowledgment.acknowledge(); // only hit if success
            if (event.getEventType().equals("TENANT-REGISTERED")){

                MessageEvent messageEvent = new MessageEvent(event.getTenantName(), mailID, "TENANT REGISTERED - " + event.getTenantName(), "YOYOYO, new guy on the block check him out", "");
                kafkaTemplate.send("notif",messageEvent);
            }
        } catch (Exception e) {
            System.err.println("Failed indexing: " + e.getMessage());
            throw new RuntimeException("Indexing failed, will retry", e); // rethrow = no ack
        }
    }
}
