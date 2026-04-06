package com.pet.manage.system.service.Impl;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.service.OtpService;
import com.pet.manage.system.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OwnerRepository ownerRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final Random random = new Random();

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
        telegramNotificationService.sendMessage(message);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        Owner owner = ownerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Owner not found with phone number: " + phoneNumber));

        if (owner.getOtpCode() == null || owner.getOtpExpiresAt() == null) {
            return Boolean.TRUE.equals(owner.getPhoneVerified());
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
}

