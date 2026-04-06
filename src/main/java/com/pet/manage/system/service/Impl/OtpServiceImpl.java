package com.pet.manage.system.service.Impl;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OwnerRepository ownerRepository;
    private final Random random = new Random();

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    @Value("${app.otp.expiry-minutes:5}")
    private long otpExpiryMinutes;

    @Override
    public void sendOtp(String phoneNumber) {
        Owner owner = ownerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Owner not found with phone number: " + phoneNumber));

        String otp = String.format("%06d", random.nextInt(1_000_000));
        owner.setOtpCode(otp);
        owner.setOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        owner.setPhoneVerified(false);
        ownerRepository.save(owner);

        String message = "OTP for " + phoneNumber + " is " + otp + ". Valid for " + otpExpiryMinutes + " minutes.";
        sendTelegram(message);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        Owner owner = ownerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Owner not found with phone number: " + phoneNumber));

        if (owner.getOtpCode() == null || owner.getOtpExpiresAt() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(owner.getOtpExpiresAt())) {
            return false;
        }

        if (!owner.getOtpCode().equals(otp)) {
            return false;
        }

        owner.setPhoneVerified(true);
        owner.setOtpCode(null);
        owner.setOtpExpiresAt(null);
        ownerRepository.save(owner);
        return true;
    }

    private void sendTelegram(String message) {
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
            throw new RuntimeException("Failed to send OTP to Telegram", ex);
        }
    }
}

