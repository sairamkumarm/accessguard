package dev.accessguard.notification_service.service;

import models.MessageEvent;

public interface MailSender {
    public void sendMail(MessageEvent messageEvent);
}
