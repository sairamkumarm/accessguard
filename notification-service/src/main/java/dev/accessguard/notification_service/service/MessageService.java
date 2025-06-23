package dev.accessguard.notification_service.service;

import models.MessageEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MailSender mailSender;

    public MessageService(MailTrapSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "notif", groupId = "notif-consumer-group")
    public void sendMails(MessageEvent messageEvent, Acknowledgment acknowledgment){
        try {
            mailSender.sendMail(messageEvent);
            acknowledgment.acknowledge();
        } catch (Exception e){
            System.err.println(e);
        }
    }
}
