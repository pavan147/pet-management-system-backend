package com.pet.manage.system.controller;

import com.pet.manage.system.dtos.request.SendOtpRequestDTO;
import com.pet.manage.system.dtos.request.VerifyOtpRequestDTO;
import com.pet.manage.system.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin("*")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequestDTO request) {
        otpService.sendOtp(request.getPhoneNumber());
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        boolean verified = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "verified", verified,
                "phoneVerified", verified,
                "message", verified ? "OTP verified successfully" : "Invalid or expired OTP"
        ));
    }
}

