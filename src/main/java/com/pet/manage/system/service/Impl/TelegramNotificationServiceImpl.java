package com.pet.manage.system.service.Impl;

import com.pet.manage.system.service.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    @Override
    public void sendMessage(String message) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }

        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = "https://api.telegram.org/bot" + botToken
                    + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMessage;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send message to Telegram", ex);
        }
    }
}

