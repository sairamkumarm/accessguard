package dev.accessguard.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.MessageEvent;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MailTrapSender implements MailSender{

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Value("${MAIL_API_KEY}")
    private String MAIL_API_KEY;

    public MailTrapSender(OkHttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMail(MessageEvent messageEvent) {
        MediaType mediaType = MediaType.parse("application/json");
        Map<String, Object> mail = new HashMap<>();
        mail.put("from", Map.of("email","accessguard@demomailtrap.co","name","Accessguard Alert"));
        mail.put("to", List.of(Map.of("email",messageEvent.getMailID(),"name", messageEvent.getReceiverName())));
        mail.put("subject",messageEvent.getSubject());
        mail.put("text",messageEvent.getBody());
        mail.put("category", "API Test");
        try {
            RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(mail), mediaType);
            Request request = new Request.Builder()
                    .url("https://send.api.mailtrap.io/api/send")
                    .method("POST", requestBody)
                    .addHeader("Authorization","Bearer "+MAIL_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println("EMAIL NOTIFICATION EMITTED: "+objectMapper.writeValueAsString(response.body()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
